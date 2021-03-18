package com.mrhi2020.ex82retrofitimageupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    ImageView iv;

    //업로드할 이미지의 절대주소를 저장하는 String 참조변수
    String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv= findViewById(R.id.iv);

        //외부저장소 사용에 대한 퍼미션
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if( checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_DENIED ){
                requestPermissions(permissions, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==100 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "외부저장소 접근 허용", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickSelect(View view) {
        //사진 or 갤러리앱을 실행해서 업로드할 사진 선택
        Intent intent= new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==10 && resultCode==RESULT_OK){
            //선택된 이미지의 URI를 얻어오기
            Uri uri= data.getData();
            if(uri != null){
                Glide.with(this).load(uri).into(iv);
                //이미지의 경로주소인 uri는 실제 물리적인 위치가 아니라 콘텐츠(DB)주소 임.

                //uri는 실제 파일의 경로주소가 아니라 안드로이드에서 사용하는 Resource 자원의 DB주소 임 - 일명 :콘텐츠 주소
                //서버에 업로드를 하려면 실제 물리적인 File 주소가 필요함
                // Uri --> 절대주소로(String) 변환
                imgPath= getRealPathFromUri(uri);
                new AlertDialog.Builder(this).setMessage(imgPath).show();
            }
       }
    }

    //Uri -- > 절대경로로 바꿔서 리턴시켜주는 메소드
    String getRealPathFromUri(Uri uri){
        String[] proj= {MediaStore.Images.Media.DATA};
        CursorLoader loader= new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor= loader.loadInBackground();
        int column_index= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result= cursor.getString(column_index);
        cursor.close();
        return  result;
    }

    public void clickUpload(View view) {
        //Retrofit 라이브러리를 이용하여 이미지 업로드

        //1)Retrofit 객체생성 - 서버로부터 echo 결과를 String으로 돌려받는
        Retrofit.Builder builder= new Retrofit.Builder();
        builder.baseUrl("http://hog2069.dothome.co.kr/");
        builder.addConverterFactory(ScalarsConverterFactory.create());
        Retrofit retrofit= builder.build();

        //2) 레트로핏 서비스객체 생성
        RetrofitService retrofitService= retrofit.create(RetrofitService.class);

        //3) File을 MultipartBody.Part 로 패킷화(포장)하여 업로드해주는 추상메소드 호출
        File file= new File(imgPath);
        RequestBody requestBody= RequestBody.create(MediaType.parse("image/*"), file); //MIME타입 및 File (택배상자)
        //택배트럭에 택배상자 넣듯이..
        MultipartBody.Part part= MultipartBody.Part.createFormData("img", file.getName(), requestBody); //식별자, 파일명, 요청Body[택배상자]

        //4) 추상메소드 실행
        Call<String> call= retrofitService.uploadImage(part);

        //5) 네트워크 작업 실행
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String s= response.body();
                new AlertDialog.Builder(MainActivity.this).setMessage(s).show();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MainActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

}//MainActivity