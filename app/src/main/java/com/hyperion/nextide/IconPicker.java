package com.hyperion.nextide;

// ပြင်ဆင်ချက် - ရိုးရိုး Activity အစား AppCompatActivity သို့ ပြောင်းလဲခြင်း
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import androidx.annotation.Nullable;

public class IconPicker extends AppCompatActivity 
{
    private final int[] iconIds = {
        R.drawable.image_11, 
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        GridView gridView = new GridView(this);
        gridView.setNumColumns(4); 
        gridView.setColumnWidth(GridView.AUTO_FIT);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setPadding(16, 16, 16, 16);
        
        ImageAdapter adapter = new ImageAdapter(this, iconIds);
        gridView.setAdapter(adapter);
        
        setContentView(gridView);
        
        // ပြင်ဆင်ချက် - AppCompat Activity ၏ ActionBar စနစ်ဖြင့် ခေါင်းစဉ်တပ်ခြင်း
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Select Icon");
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("selected_icon_id", iconIds[position]);
                // ပြင်ဆ disguise - AppCompatActivity နှင့် ကိုက်ညီသော RESULT_OK သုံးစွဲခြင်း
                setResult(AppCompatActivity.RESULT_OK, returnIntent);
                finish(); 
            }
        });
    }

    private static class ImageAdapter extends BaseAdapter {
        private final Context context;
        private final int[] imageIds;

        public ImageAdapter(Context context, int[] imageIds) {
            this.context = context;
            this.imageIds = imageIds;
        }

        @Override
        public int getCount() {
            return imageIds.length;
        }

        @Override
        public Object getItem(int position) {
            return imageIds[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                int size = (int) (64 * context.getResources().getDisplayMetrics().density);
                imageView.setLayoutParams(new GridView.LayoutParams(size, size));
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(imageIds[position]);
            return imageView;
        }
    }
}
