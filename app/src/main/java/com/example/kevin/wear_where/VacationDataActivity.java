package com.example.kevin.wear_where;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kevin.wear_where.AsyncTask.PlannerAST;
import com.example.kevin.wear_where.WundergroundData.Planner.PlannerObject;
import com.example.kevin.wear_where.wear.Clothing;
import com.example.kevin.wear_where.wear.ClothingActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class VacationDataActivity extends AppCompatActivity {

    public final static String UPPERBODY = "com.example.kevin.wear_where.UpperBody";
    public final static String LOWERBODY = "com.example.kevin.wear_where.LowerBody";
    public final static String SHOES = "com.example.kevin.wear_where.Shoes";

    // Variables containing information passed from MainActivity
    private String city, state, leaveTime, returnTime;

    // Linear Layout we are adding timeframe layout to
    private LinearLayout tfList;

    // Bundles to be passed to clothing suggestion
    private Bundle upperbody, lowerbody, shoes;
    private Bundle upperbodyConsider, lowerbodyConsider, shoesConsider;

    InputStream firstFile;
    InputStream secondFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_data);


        // Assign views to variables
        TextView destinationTV = (TextView) findViewById(R.id.Destination);
        TextView dateTV = (TextView) findViewById(R.id.Date);

        // Button to return back to MainActivity
        Button returnButton = (Button) findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get extra variables passed from MainActivity
        city = getIntent().getExtras().getString("city");
        state = getIntent().getExtras().getString("state");
        leaveTime = getIntent().getExtras().getString("leaveDate");
        returnTime = getIntent().getExtras().getString("returnDate");

        // Assign text to views
        destinationTV.setText(city + ", " + state);
        dateTV.setText(leaveTime + " - " + returnTime);

        GetData();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Instantiate clothing from main/assets/ folder
        AssetManager assetManager = this.getResources().getAssets();

        try {
            firstFile = assetManager.open("herClothes.txt");
            secondFile = assetManager.open("hisClothes.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: Do validation in MainActivity that'll determine if this activity getes loaded
//        if (city == null || state == null) {
//            Toast.makeText(this, "No valid location found", Toast.LENGTH_LONG).show();
//        } else if (leaveTime == null) {
//            Toast.makeText(this, "No leave date found", Toast.LENGTH_LONG).show();
//        } else if (returnTime == null) {
//            Toast.makeText(this, "No return date found", Toast.LENGTH_LONG).show();
//        }
    }

    // Calculates the duration of stay roughly estimating all months to be 30days
    public int CalculateDuration(String leaving, String returning) {
        int tempLeaveDay = GetDay(leaving);
        int tempReturnDay = GetDay(returning);
        int tempLeaveMonth = GetMonth(leaving);
        int tempReturnMonth = GetMonth(returning);
        int tempLeaveYear = GetYear(leaving);
        int tempReturnYear = GetYear(returning);

        int tempDuration = 0;
        while(tempLeaveMonth != tempReturnMonth || tempLeaveYear != tempReturnYear) {
            // Calculate all days of the vacation months where vacation months reside in the same year
            if (tempReturnMonth > tempLeaveMonth) {
                // Duration of at least a month
                while (tempReturnMonth > tempLeaveMonth) {
                    tempDuration += DaysInMonth(tempLeaveMonth);
                    tempLeaveMonth++;
                }
            }
            // Calculate all days of the vacation months where vacation extends to a new year
            else if (tempReturnMonth <= tempLeaveMonth && tempReturnYear > tempLeaveYear) {
                if (tempLeaveMonth < 12) {
                    tempDuration += DaysInMonth(tempLeaveMonth);
                    tempLeaveMonth++;
                } else if (tempLeaveMonth == 12) {
                    tempDuration += DaysInMonth(tempLeaveMonth);
                    tempLeaveMonth = 1;
                    tempLeaveYear++;
                }
            }
        }
        // Subtract the days before that you have not left yet from the first month and add the remaining days of the return month
        tempDuration = tempDuration - tempLeaveDay + tempReturnDay;

        return tempDuration;
    }

    // Returns the day from a given string
    public int GetDay(String date) {
        String temp = "" + date.charAt(3) + date.charAt(4);
        return Integer.parseInt(temp);
    }

    // Returns the month of a given string
    public int GetMonth(String date) {
        String temp = "" + date.charAt(0) + date.charAt(1);
        return Integer.parseInt(temp);
    }

    // Returns the year of a given string
    public int GetYear(String date) {
        String temp = "" + date.charAt(6) + date.charAt(7) + date.charAt(8) + date.charAt(9);
        return Integer.parseInt(temp);
    }

    // Calculates an appropriate timeframe for a given duration
    public int CalculateTimeFrame(int duration) {
        if (duration >= 36) {
            // Return 30days timeframe if duration is from 1-5months
            return 30;
        } else if (duration >= 8 && duration <= 35) {
            // Return 7days timeframe if duration is from 1-5weeks
            return 7;
        } else return 1;
    }

    // Returns the number of days in a month
    public int DaysInMonth(int month) {
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            return 31;
        } else if (month == 2) {
            return 28;
        } else return 30;
    }

    // Creates the beginning timeframes given the length of timeframe and vacation dates
    public String[] CreateTimeFrames(int timeFrameLength, String start, String end, int frames) {
        int tempStartMonth = GetMonth(start);
        int tempStartDay = GetDay(start);
        int tempStartYear = GetYear(start);
        int tempEndMonth = GetMonth(end);
        int tempEndDay = GetDay(end);
        int tempEndYear = GetYear(end);
        String tempTimeFrames[] = new String[frames + 1];

        // Add first beginning timeframe to array
        tempTimeFrames[0] = FormatDate(tempStartMonth, tempStartDay, tempStartYear);

        // Initialize index for after first timeframe
        int index = 1;

        // Add every other beginning timeframe to array
        while (tempStartMonth != tempEndMonth || tempStartDay != tempEndDay || tempStartYear != tempEndYear) {
            // Increment date to next timeframe
            tempStartDay += timeFrameLength;

            if(tempStartDay > DaysInMonth(tempStartMonth)){
                // If day exceeds number of days in month then move to the next month
                tempStartDay -= DaysInMonth(tempStartMonth);
                tempStartMonth++;

                if(tempStartMonth > 12){
                    tempStartMonth -= 12;
                    tempStartYear++;
                }
            }

            // Same month and year but day exceeds the return day
            if(tempStartMonth == tempEndMonth && tempStartYear == tempEndYear && tempStartDay > tempEndDay){
                tempStartDay = tempEndDay;
            }
            // Day extends into a new year when return day is still in previous year
            else if(tempStartYear > tempEndYear){
                tempStartMonth = tempEndMonth;
                tempStartDay = tempEndDay;
                tempStartYear = tempEndYear;
            }
            // Day extends to a new month in the same year when return day is still in previous month
            else if(tempStartYear == tempEndYear && tempStartMonth > tempEndMonth){
                tempStartMonth = tempEndMonth;
                tempStartDay = tempEndDay;
                tempStartYear = tempEndYear;
            }

            // Add timeframe to index and increment index
            tempTimeFrames[index] = FormatDate(tempStartMonth, tempStartDay, tempStartYear);
            index++;
        }

        return tempTimeFrames;
    }

    // Calculates the number of timeframes given the duration and timeframe length of the vacation
    public int CalculateNumberofTimeFrames(int duration, int frameLength){
        int temp;

        if(duration % frameLength != 0){
            temp = (duration / frameLength) + 1;
        }
        else temp = duration / frameLength;

        return temp;
    }

    public String FormatAPIDate(int month, int day) {
        String tempDate;
        if (month < 10) {
            tempDate = "0" + Integer.toString(month);
        } else tempDate = Integer.toString(month);

        if (day < 10) {
            tempDate = tempDate + "0" + Integer.toString(day);
        } else tempDate = tempDate + Integer.toString(day);

        return tempDate;
    }

    public String FormatDate(int month, int day, int year) {
        String tempDate;
        if (month < 10) {
            tempDate = "0" + Integer.toString(month);
        } else tempDate = Integer.toString(month);

        tempDate = tempDate + "/";

        if (day < 10) {
            tempDate = tempDate + "0" + Integer.toString(day);
        } else tempDate = tempDate + Integer.toString(day);

        tempDate = tempDate + "/" + Integer.toString(year);

        return tempDate;
    }

    // Setup Onclicklistener for the clothing suggestion button for each time frame
    public void SetSuggestionButton(final PlannerObject plannerObject, View timeframe){
        Button suggestion = (Button) timeframe.findViewById(R.id.Suggestion);
        suggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initialize Bundles to be used for clothing suggestion
                upperbody = new Bundle();
                lowerbody = new Bundle();
                shoes = new Bundle();
                upperbodyConsider = new Bundle();
                lowerbodyConsider = new Bundle();
                shoesConsider = new Bundle();
                Clothing clothesObject = new Clothing(firstFile, secondFile);
                Intent clothingActivity = new Intent(VacationDataActivity.this, ClothingActivity.class);
                // Add Hot clothing to Bundle
                if(plannerObject.getTempOverNinetyChance() >= plannerObject.getTempOverSixtyChance() && plannerObject.getTempOverNinetyChance() >= plannerObject.getFreezingChance() && plannerObject.getTempOverNinetyChance() >= plannerObject.getBelowFreezingChance()) {
                    AddHot(clothesObject, "Plan");
                    if(plannerObject.getTempOverSixtyChance() >= 30){
                        AddWarm(clothesObject, "Consider");
                    }
                    if(plannerObject.getFreezingChance() >= 30){
                        AddChilly(clothesObject, "Consider");
                    }
                    if(plannerObject.getBelowFreezingChance() >= 30){
                        AddFreezing(clothesObject, "Consider");
                    }
                }
                // Add Warm clothing to Bundle
                else if(plannerObject.getTempOverSixtyChance() >= plannerObject.getTempOverNinetyChance() && plannerObject.getTempOverSixtyChance() >= plannerObject.getFreezingChance() && plannerObject.getTempOverSixtyChance() >= plannerObject.getBelowFreezingChance()) {
                    AddWarm(clothesObject, "Plan");
                    if(plannerObject.getTempOverNinetyChance() >= 30){
                        AddHot(clothesObject, "Consider");
                    }
                    if(plannerObject.getFreezingChance() >= 30){
                        AddChilly(clothesObject, "Consider");
                    }
                    if(plannerObject.getBelowFreezingChance() >= 30){
                        AddFreezing(clothesObject, "Consider");
                    }
                }

                // Add Chilly clothing to Bundle
                else if(plannerObject.getFreezingChance() >= plannerObject.getTempOverNinetyChance() && plannerObject.getFreezingChance() >= plannerObject.getTempOverSixtyChance() && plannerObject.getFreezingChance() >= plannerObject.getBelowFreezingChance()) {
                    AddChilly(clothesObject, "Plan");
                    if(plannerObject.getTempOverNinetyChance() >= 30){
                        AddHot(clothesObject, "Consider");
                    }
                    if(plannerObject.getTempOverSixtyChance() >= 30){
                        AddWarm(clothesObject, "Consider");
                    }
                    if(plannerObject.getBelowFreezingChance() >= 30){
                        AddFreezing(clothesObject, "Consider");
                    }
                }

                // Add Freezing clothing to Bundle
                else if(plannerObject.getBelowFreezingChance() >= plannerObject.getTempOverNinetyChance() && plannerObject.getBelowFreezingChance() >= plannerObject.getTempOverSixtyChance() && plannerObject.getBelowFreezingChance() >= plannerObject.getFreezingChance()) {
                    AddFreezing(clothesObject, "Plan");
                    if(plannerObject.getTempOverNinetyChance() >= 30){
                        AddHot(clothesObject, "Consider");
                    }
                    if(plannerObject.getTempOverSixtyChance() >= 30){
                        AddWarm(clothesObject, "Consider");
                    }
                    if(plannerObject.getFreezingChance() >= 30){
                        AddChilly(clothesObject, "Consider");
                    }
                }

                // Pass bundle to Clothing Intent
                clothingActivity.putExtra(UPPERBODY + ".Bundle", upperbody);
                clothingActivity.putExtra(LOWERBODY + ".Bundle", lowerbody);
                clothingActivity.putExtra(SHOES + ".Bundle", shoes);
                clothingActivity.putExtra(UPPERBODY + ".BundleConsider", upperbodyConsider);
                clothingActivity.putExtra(LOWERBODY + ".BundleConsider", lowerbodyConsider);
                clothingActivity.putExtra(SHOES + ".BundleConsider", shoesConsider);
                startActivity(clothingActivity);
            }
        });
    }

    // Adds the Hot clothing suggestion to Bundle to be passed to next activity
    public void AddHot(Clothing clothesObject, String bundleDest){
        ArrayList<String> upperbody = clothesObject.getUpperBody("80");
        String[] upperbodyList = new String[upperbody.size()];
        for (int i = 0; i < upperbody.size(); i++){
            upperbodyList[i] = upperbody.get(i);
        }
        // Place upperbody clothing into bundle
        if(bundleDest == "Plan") {
            this.upperbody.putStringArray(UPPERBODY + ".Hot", upperbodyList);
        }else if(bundleDest == "Consider"){
            this.upperbodyConsider.putStringArray(UPPERBODY + ".Hot", upperbodyList);
        }
        // Lowerbody list
        ArrayList<String> lowerbody = clothesObject.getLowerBody("80");
        String[] lowerbodyList = new String[lowerbody.size()];
        for (int i = 0; i < lowerbody.size(); i++){
            lowerbodyList[i] = lowerbody.get(i);
        }
        if(bundleDest == "Plan") {
            this.lowerbody.putStringArray(LOWERBODY + ".Hot", lowerbodyList);
        }else if(bundleDest == "Consider"){
            this.lowerbodyConsider.putStringArray(LOWERBODY + ".Hot", upperbodyList);
        }
        // Shoes list
        ArrayList<String> shoes = clothesObject.getShoes("80");
        String[] shoesList = new String[shoes.size()];
        for (int i = 0; i < shoes.size(); i++){
            shoesList[i] = shoes.get(i);
        }
        if(bundleDest == "Plan") {
            this.shoes.putStringArray(SHOES + ".Hot", shoesList);
        }else if(bundleDest == "Consider"){
            this.shoesConsider.putStringArray(SHOES + ".Hot", upperbodyList);
        }
    }

    // Adds the Warm clothing suggestion to Bundle to be passed to next activity
    public void AddWarm(Clothing clothesObject, String bundleDest) {
        ArrayList<String> upperbody = clothesObject.getUpperBody("60");
        String[] upperbodyList = new String[upperbody.size()];
        for (int i = 0; i < upperbody.size(); i++) {
            upperbodyList[i] = upperbody.get(i);
        }
        if (bundleDest == "Plan") {
            this.upperbody.putStringArray(UPPERBODY + ".Warm", upperbodyList);
        } else if (bundleDest == "Consider") {
            this.upperbodyConsider.putStringArray(UPPERBODY + ".Warm", upperbodyList);
        }
        // Lowerbody list
        ArrayList<String> lowerbody = clothesObject.getLowerBody("60");
        String[] lowerbodyList = new String[lowerbody.size()];
        for (int i = 0; i < lowerbody.size(); i++) {
            lowerbodyList[i] = lowerbody.get(i);
        }
        if (bundleDest == "Plan") {
            this.lowerbody.putStringArray(LOWERBODY + ".Warm", lowerbodyList);
        } else if (bundleDest == "Consider") {
            this.lowerbodyConsider.putStringArray(LOWERBODY + ".Warm", upperbodyList);
        }
        // Shoes list
        ArrayList<String> shoes = clothesObject.getShoes("60");
        String[] shoesList = new String[shoes.size()];
        for (int i = 0; i < shoes.size(); i++) {
            shoesList[i] = shoes.get(i);
        }
        if (bundleDest == "Plan") {
            this.shoes.putStringArray(SHOES + ".Warm", shoesList);
        } else if (bundleDest == "Consider") {
            this.shoesConsider.putStringArray(SHOES + ".Warm", shoesList);
        }
    }

    // Adds the Chilly clothing suggestion to Bundle to be passed to next activity
    public void AddChilly(Clothing clothesObject, String bundleDest){
        ArrayList<String> upperbody = clothesObject.getUpperBody("40");
        String[] upperbodyList = new String[upperbody.size()];
        for (int i = 0; i < upperbody.size(); i++){
            upperbodyList[i] = upperbody.get(i);
        }
        if (bundleDest == "Plan") {
            this.upperbody.putStringArray(UPPERBODY + ".Chilly", upperbodyList);
        }else if (bundleDest == "Consider") {
            this.upperbodyConsider.putStringArray(UPPERBODY + ".Chilly", upperbodyList);
        }
        // Lowerbody list
        ArrayList<String> lowerbody = clothesObject.getLowerBody("40");
        String[] lowerbodyList = new String[lowerbody.size()];
        for (int i = 0; i < lowerbody.size(); i++){
            lowerbodyList[i] = lowerbody.get(i);
        }
        if (bundleDest == "Plan") {
            this.lowerbody.putStringArray(LOWERBODY + ".Chilly", lowerbodyList);
        }else if (bundleDest == "Consider") {
            this.lowerbodyConsider.putStringArray(LOWERBODY + ".Chilly", lowerbodyList);
        }
        // Shoes list
        ArrayList<String> shoes = clothesObject.getShoes("40");
        String[] shoesList = new String[shoes.size()];
        for (int i = 0; i < shoes.size(); i++){
            shoesList[i] = shoes.get(i);
        }
        if (bundleDest == "Plan") {
            this.shoes.putStringArray(SHOES + ".Chilly", shoesList);
        }else if (bundleDest == "Consider") {
            this.shoesConsider.putStringArray(SHOES + ".Chilly", shoesList);

        }
    }

    // Adds the Freezing clothing suggestion to Bundle to be passed to next activity
    public void AddFreezing(Clothing clothesObject, String bundleDest){
        ArrayList<String> upperbody = clothesObject.getUpperBody("0");
        String[] upperbodyList = new String[upperbody.size()];
        for (int i = 0; i < upperbody.size(); i++){
            upperbodyList[i] = upperbody.get(i);
        }
        if (bundleDest == "Plan") {
            this.upperbody.putStringArray(UPPERBODY + ".Freezing", upperbodyList);
        }else if (bundleDest == "Consider") {
            this.upperbodyConsider.putStringArray(UPPERBODY + ".Freezing", upperbodyList);
        }
        // Lowerbody list
        ArrayList<String> lowerbody = clothesObject.getLowerBody("0");
        String[] lowerbodyList = new String[lowerbody.size()];
        for (int i = 0; i < lowerbody.size(); i++){
            lowerbodyList[i] = lowerbody.get(i);
        }
        if (bundleDest == "Plan") {
            this.lowerbody.putStringArray(LOWERBODY + ".Freezing", lowerbodyList);
        }else if (bundleDest == "Consider") {
            this.lowerbodyConsider.putStringArray(LOWERBODY + ".Freezing", lowerbodyList);
        }
        // Shoes list
        ArrayList<String> shoes = clothesObject.getShoes("0");
        String[] shoesList = new String[shoes.size()];
        for (int i = 0; i < shoes.size(); i++){
            shoesList[i] = shoes.get(i);
        }
        if (bundleDest == "Plan") {
            this.shoes.putStringArray(SHOES + ".Freezing", shoesList);
        }else if (bundleDest == "Consider") {
            this.shoesConsider.putStringArray(SHOES + ".Freezing", shoesList);
        }
    }

    // Displays the data of the given timeframe
    public void DisplayResults(PlannerObject plannerObject, View timeframe) {
        TextView tempText;
        tempText = (TextView) timeframe.findViewById(R.id.AboveNinetyPercent);
        tempText.setText("" + plannerObject.getTempOverNinetyChance() + "%");
        tempText = (TextView) timeframe.findViewById(R.id.AboveSixtyPercent);
        tempText.setText("" + plannerObject.getTempOverSixtyChance() + "%");
        tempText = (TextView) timeframe.findViewById(R.id.AboveFreezingPercent);
        tempText.setText("" + plannerObject.getFreezingChance() + "%");
        tempText = (TextView) timeframe.findViewById(R.id.BelowFreezingPercent);
        tempText.setText("" + plannerObject.getBelowFreezingChance() + "%");
        tempText = (TextView) timeframe.findViewById(R.id.RainPercent);
        tempText.setText("" + plannerObject.getRainChance() + "%");
        tempText = (TextView) timeframe.findViewById(R.id.WindyPercent);
        tempText.setText("" + plannerObject.getWindyChance() + "%");
        tempText = (TextView) timeframe.findViewById(R.id.HumidPercent);
        tempText.setText("" + plannerObject.getHumidChance() + "%");
    }

    public void DisplayDate(String start, String end, TextView view){
        view.setText("" + start + " - " + end);
    }

    public void GetData(){
        // Assign the Linear Layout to variable
        tfList = (LinearLayout) findViewById(R.id.TimeFrameList);
        // Get the length of the vacation
        int vacationDuration = CalculateDuration(leaveTime, returnTime);
        // Get the suitable timeframe length
        int vacationTimeFrameLength = CalculateTimeFrame(vacationDuration);
        // Get the number of timeframes given the duration and timeframe length
        int numofTimeFrames = CalculateNumberofTimeFrames(vacationDuration, vacationTimeFrameLength);
        // Get an array of the timeframes being looked up
        final String timeFrames[] = CreateTimeFrames(vacationTimeFrameLength, leaveTime, returnTime, numofTimeFrames);

        for(int i = 1; i < timeFrames.length; i++){
            // Layout Inflater
            LayoutInflater li = LayoutInflater.from(this);
            // Timeframe view to be added to linear layout list
            final View timeframe = li.inflate(R.layout.timeframe, null);
            // Update text for date
            TextView tempTextView = (TextView) timeframe.findViewById(R.id.Date);
            DisplayDate(timeFrames[i - 1], timeFrames[i], tempTextView);

            new PlannerAST(FormatAPIDate(GetMonth(timeFrames[i - 1]), GetDay(timeFrames[i - 1])), FormatAPIDate(GetMonth(timeFrames[i]), GetDay(timeFrames[i])), city, state){
                @Override
                protected void onPostExecute(PlannerObject result){
                    // Updates condition chances
                    DisplayResults(result, timeframe);
                    // Setup clothing suggestion button
                    SetSuggestionButton(result, timeframe);
                    // Add timeframe view to
                    tfList.addView(timeframe);
                }
            }.execute();
        }
    }
}
