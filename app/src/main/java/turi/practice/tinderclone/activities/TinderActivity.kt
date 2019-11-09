package turi.practice.tinderclone.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import kotlinx.android.synthetic.main.activity_main.*
import turi.practice.tinderclone.R

class TinderActivity : AppCompatActivity() {

    private var al = ArrayList<String>()
    private var arrayAdapter: ArrayAdapter<String>? = null
    private var i = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //add the view via xml or programmatically

        al.add("php");
        al.add("c");
        al.add("python");
        al.add("java");

        //choose your favorite adapter
        arrayAdapter = ArrayAdapter(this,
            R.layout.item,
            R.id.helloText, al);

        //set the listener and the adapter
        frame.adapter = arrayAdapter
        frame.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {
            override fun removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                al.removeAt(0);
                arrayAdapter?.notifyDataSetChanged();

            }

            override fun onLeftCardExit(p0: Any?) {
                Toast.makeText(this@TinderActivity, "Left!", Toast.LENGTH_SHORT).show();

            }

            override fun onRightCardExit(p0: Any?) {
                Toast.makeText(this@TinderActivity, "Right!", Toast.LENGTH_SHORT).show();

            }

            override fun onAdapterAboutToEmpty(p0: Int) {
                al.add("XML $i")
                arrayAdapter?.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++

            }

            override fun onScroll(p0: Float) {

            }
        })
    }

    companion object{
        fun newIntent(context: Context?) = Intent(context, TinderActivity::class.java)
    }

}





