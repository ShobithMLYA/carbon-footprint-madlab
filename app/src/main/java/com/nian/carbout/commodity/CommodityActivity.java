package com.nian.carbout.commodity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.nian.carbout.DataBaseHelper;
import com.nian.carbout.R;
import com.nian.carbout.commodity_Search.CommoditySearchActivity;
import com.nian.carbout.commodity_Search.list_item;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import info.debatty.java.stringsimilarity.JaroWinkler;

public class CommodityActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 200;
    private static final int REQEUST_QR = 100;
    private ArrayList<shop_list> Commodity_item = new ArrayList<>();
    private ArrayList<list_item> Commodity_DB = new ArrayList<>();
    private DataBaseHelper dataHelper;
    private SQLiteDatabase db;
    private int date=0,have_date=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity);

        try{
            getSupportActionBar().hide();
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }





        CardView card = findViewById(R.id.cardView_of_QR);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CommodityActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                } else {
                    Intent intent = new Intent(CommodityActivity.this, QR_Activity.class);
                    startActivityForResult(intent, REQEUST_QR);
                }

            }
        });

        CardView card_search = findViewById(R.id.cardView_of_search);
        card_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startActivity(new Intent(v.getContext(), CommoditySearchActivity.class));
            }
        });

        setDataBase();
    }

    public void dialogShow(Context context, String title, String notify)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(notify)
                .setNeutralButton("??????",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                CommodityActivity.this.finish();//??????????????????????????????activity

                    }
                })
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void decode_QR(String str)
    {
        String title = "????????????",notify;
        Commodity_item.clear();

        str = str.trim();//??????????????????

        //???????????????regex
        String regex_left = "^[a???zA-z0-9]{10}[0-9]{7}[0-9]{4}[0-9a-z]{8}[0-9a-z]{8}[0-9]{8}[0-9]{8}.{24}(:.{10}:[0-9]+:[0-9]+:[0-9]+(:.+:[0-9.]+:[0-9.]+)*:*)?";
        String regex_right = "^\\*\\*(:*.+:[0-9.]+:[0-9.]+)*:*";

        if(str.matches(regex_left))//??????QR code
        {
            have_date=1;

            Toast.makeText(getApplicationContext(),"??????QR code????????????",Toast.LENGTH_LONG).show();

            //String receipt_number = str.substring(0,10);//?????????????????????????????????
            String receipt_date = Integer.parseInt(str.substring(10,13))+1911+str.substring(13,15)+str.substring(15,17);

            date = Integer.parseInt(receipt_date);

            String[] split_item = str.split(":");

            if(split_item.length>2)//????????????
            {
                for(int i=5;i<split_item.length;i+=3)
                {
                    Commodity_item.add(new shop_list(split_item[i],Float.parseFloat(split_item[i+1])));
                }
            }

        }
        else if(str.matches(regex_right))//?????? QR code
        {
            have_date=0;

            Toast.makeText(getApplicationContext(),"??????QR code????????????",Toast.LENGTH_LONG).show();

            if(str.equals("**"))
            {
                notify="??????QR code???????????????";
                dialogShow(CommodityActivity.this, title, notify);
                return;
            }


            //??????????????????
            if(str.indexOf(2)==':')//???????????????**:
                str=str.replace("**:","");
            else//??????**
                str=str.replace("**","");


            String[] split_item = str.split(":");


            for(int i=0;i<split_item.length;i++)
            {
                Log.d("test", i+":"+split_item[i]);
            }

            Log.d("length", split_item.length+"");

            for(int i=0;i<split_item.length;i+=3)
            {
                Commodity_item.add(new shop_list(split_item[i],Float.parseFloat(split_item[i+1])));
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),"??????????????????????????????QR code",Toast.LENGTH_LONG).show();
            return;
        }

        search_in_DB();
    }

    public void search_in_DB()
    {
        int unit_change,index,max_index=0;
        double[] similarity = new double[Commodity_DB.size()];
        double max=0f;
        String title = "????????????";
        StringBuilder notify = new StringBuilder();
        JaroWinkler jw = new JaroWinkler();//??????java-string-similarity??????


        if(have_date==1) notify.append("???????????????")
                .append(date/10000)
                .append("/")
                .append(date%10000/100)
                .append("/")
                .append(date%100)
                .append("\n\n");

        //Commodity_item;
        for(shop_list item: Commodity_item)
        {
            //Log.d("shop list", item.getName()+ " "+ item.getNumber());
            index=0;

            //????????????????????????????????????
            for(list_item item_DB : Commodity_DB)
            {
                similarity[index] = jw.similarity(item.getName(),item_DB.getName());
                //Log.d("?????????"+item.getName(), item_DB.getName()+ ":" + similarity[index]);
                index++;
            }

            //?????????????????????
            for(int i=0;i<similarity.length;i++)
            {
                if(max<similarity[i])
                {
                    max = similarity[i];
                    max_index = i;
                }
            }

            Log.d("MAX Similarity", Commodity_DB.get(max_index).getName()+" : "+max);

            notify.append(item.getName())
                    .append(" * ")
                    .append(item.getNumber())
                    .append("    ");

            if(max <= 0.75f) notify.append("????????????\n");//???????????????
            else notify.append(Commodity_DB.get(max_index).getCo2()).append(Commodity_DB.get(max_index).getUnit()).append("\n");

            //??????DB???????????????????????????
            unit_change = (Commodity_DB.get(max_index).getUnit().equals("kg"))?1000:1;
            //??????DB
            dataHelper.append(
                    db, (int)(Commodity_DB.get(max_index).getCo2()*unit_change*item.getNumber())
                    ,Commodity_DB.get(max_index).getName(),date);
        }

        dialogShow(CommodityActivity.this, title, notify.toString());
    }

    public void setDataBase()
    {
        dataHelper = new DataBaseHelper(this, "resource.db", null, 1);
        db = dataHelper.getWritableDatabase();

        //??????????????????SQLite????????????????????????
        Cursor c = db.rawQuery("SELECT * FROM Commodity", null);
        c.moveToFirst();

        float testCO2;
        String unit="",number="",total_string;

        for(int i = 0; i < c.getCount(); i++) {
            //????????????????????????????????????

            try{
                total_string = c.getString(3);
                if(total_string.matches("[0-9]+kg"))
                {
                    number = total_string.replace("kg","");
                    unit = "kg";
                }
                else if(total_string.matches("[0-9]+g"))
                {
                    number = total_string.replace("g","");
                    unit = "g";
                }

                testCO2 = Integer.parseInt(number);
            }
            catch (NumberFormatException e)
            {
                c.moveToNext();
                continue;
            }

            //Log.d("Detail", c.getString(1)+":"+c.getString(3));
            String name = c.getString(1);
            String spec = c.getString(2);


            if(!spec.equals("-"))
            {
                name = name + spec;
            }

            Log.d("Item", name);


            Commodity_DB.add(new list_item(name, testCO2,unit));
            //??????????????????
            c.moveToNext();
        }

        c.close();


        dataHelper = new DataBaseHelper(this, "co2.sqlite",null, 1);
        db = dataHelper.getWritableDatabase();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQEUST_QR && resultCode == RESULT_OK) {
            if (data != null) {
                final Barcode barcode = data.getParcelableExtra("barcode");
                decode_QR(barcode.displayValue);
            }
        }
    }
}