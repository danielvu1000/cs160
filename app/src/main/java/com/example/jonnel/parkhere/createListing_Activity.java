package com.example.jonnel.parkhere;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class createListing_Activity extends AppCompatActivity {
    Button createListing;
    EditText address_text;
    EditText price_text;
    String address;
    String zip;
    double price;
    private static String key = " ";
    private String sTime;
    private String eTime;
    private String sDate;
    private String eDate;
    private AutoCompleteTextView ZipView;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        final DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_create_listing_);

        Bundle pastInstance = getIntent().getExtras();
        if(pastInstance !=null) {
            TextView startTime = (TextView) findViewById(R.id.startTime);
            startTime.setText(pastInstance.getString("startTime"));
            sTime= pastInstance.getString("startTime");

            TextView endTime = (TextView) findViewById(R.id.endTime);
            endTime.setText(pastInstance.getString("endTime"));
            eTime= pastInstance.getString("endTime");

            TextView startDate = (TextView) findViewById(R.id.startDate);
            startDate.setText(pastInstance.getString("beginDate"));
            sDate= pastInstance.getString("beginDate");

            TextView endDate = (TextView) findViewById(R.id.endDate);
            endDate.setText(pastInstance.getString("endDate"));
            eDate= pastInstance.getString("endDate");
        }
        createListing = findViewById(R.id.submit);
        address_text = findViewById(R.id.address);
        price_text = findViewById(R.id.price);
        ZipView= (AutoCompleteTextView) findViewById(R.id.zip);
        String add = address_text.getText().toString();
        String pri = price_text.getText().toString();
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        String[] state = {"AK",  "AL", "AR", "AZ", "CA", "CO", "CT" ,"DE" ,"FL" ,"GA" ,"HI" ,"IA" ,"ID" ,"IL" ,"IN" ,"KS" ,"KY" ,"LA" ,"MA" ,"MD" ,"ME" ,"MI" ,"MN",
                "MO" ,"MS" ,"MT" ,"NC" ,"ND" ,"NE" ,"NH" ,"NJ" ,"NM", "NV" ,"NY" ,"OH" ,"OK" ,"OR" ,"PA" ,"RI" ,"SC" ,"SD" ,"TN" ,"TX" ,"UT" ,"VA" ,
                "VT" ,"WA" ,"WI" ,"WV" ,"WY"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(createListing_Activity.this,android.R.layout.simple_spinner_item, state);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        zip= ZipView.getText().toString();
        Spinner mySpinner=(Spinner) findViewById(R.id.spinner);
        final String stateInput = mySpinner.getSelectedItem().toString();


        if ( add.trim().equals("") || pri.trim().equals("")){
            price_text.setError(getString(R.string.error_field_required));
            address_text.setError(getString(R.string.error_field_required));
        }
        if(!add.trim().equals("") && !pri.trim().equals("")){
            price_text.setError(null);
            address_text.setError(null);
            //createListing.setEnabled(true);
        }
        if(TextUtils.isEmpty(zip) || zipTest(zip) == false){
            ZipView.setError(getString(R.string.error_field_required));
        }
        if(!TextUtils.isEmpty(zip) || !zipTest(zip) == false){

            ZipView.setError(null);
        }
        createListing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> userListings = new HashMap<>();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid="";
                if(user!=null) {
                     uid = user.getUid();
                }
                //THIS IS NOT A FIX FOR HAVING PRICE NULL AND IT DOES NOT EVEN HANDLE ADDRESS NULL PLZ FIX
                if(!price_text.getText().toString().equals("")) {
                    price = Double.parseDouble(price_text.getText().toString());
                }
                else
                {
                    price = 0;
                }

                zip = ZipView.getText().toString();
                address = address_text.getText().toString();




                NumberFormat formatter = NumberFormat.getCurrencyInstance();
                formatter.format(price);


                DatabaseReference userRef = dataRef.child("User Id: " + uid);
                DatabaseReference listingRef = userRef.push();
                key = listingRef.getKey();


                PSpot spot = new PSpot(price, address,stateInput,zip, sDate, eDate, sTime, eTime, true, uid, null,0, getKey());

               // if (address_text != null && price_text != null) {
                if(validPrice(price) && validAddress(address) && PriceBound(price)){
                    Context context = getApplicationContext();
                    CharSequence message = "You created a new parking spot"
                            + " " + "at" + " " + address + " " + "for" + " " + formatter.format(price) + " dollars";
                    int duration = Toast.LENGTH_LONG;
                    Toast.makeText(context, message, duration).show();

                    System.out.println("You have successfully created a new parking spot!");
                    System.out.println("Price:" + " " + "$" + formatter.format(price));
                    System.out.println("Address:" + " " + address);
                } else {

                    Context context = getApplicationContext();
                    CharSequence message = "Invalid parking spot listing";
                    int duration = Toast.LENGTH_LONG;
                    Toast.makeText(context, message, duration).show();
                }



                listingRef.setValue(spot);
                dataRef.child("User Id: " + uid).child(key).child("Original Information").child("Start Time").setValue(sTime);
                dataRef.child("User Id: " + uid).child(key).child("Original Information").child("End Time").setValue(eTime);
                dataRef.child("User Id: " + uid).child(key).child("Original Information").child("Start Date").setValue(sDate);
                dataRef.child("User Id: " + uid).child(key).child("Original Information").child("End Date").setValue(eDate);
                menu();

            }
        });
        Intent data = new Intent("name").putExtra("key", key);
        LocalBroadcastManager.getInstance(this).sendBroadcast(data);



    }
    public static String getKey(){
        return key;
    }
    public boolean validPrice(double price){
        if(price <= 0){
            return false;
        }
        return true;
    }
    public boolean validAddress(String add){
        if(add.length() > 6 && !add.isEmpty()){
            return true;
        }
        return false;

    }
    public boolean PriceBound(double price) {
        if( price > 10000 ) {
         return false;
        }
        return true;
    }
    private void menu(){
        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }
    private boolean zipTest(String zip)
    {
        boolean ziptest = false;
        String regex = "^[0-9]{5}(?:-[0-9]{4})?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(zip);
        return matcher.matches();
    }
}