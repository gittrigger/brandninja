package com.havenskys.thescoopseattle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class LoadingListView extends Activity {

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		LinearLayout lHeader = new LinearLayout(this);
        lHeader.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
        tv.setText("\nLoading\n");
        tv.setTextSize(22);
		tv.setShadowLayer( (float) 2.0, (float) 0.5, (float) 0.5, (int) Color.BLACK);
		tv.setTextColor(Color.WHITE);
		lHeader.setPadding(5, 10, 5, 10);
        lHeader.addView(tv);
		
        setContentView(lHeader);
		setTitle("-100");
        
		//Intent goFish = new Intent(this, listView.class);
		//goFish.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//getParent().startActivity(goFish);
		//startActivity(goFish);
	}
}
