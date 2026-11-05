package com.example.stampcleaner
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
@Composable fun StampCleanerScreen(){
 val c=LocalContext.current
 var src by remember{mutableStateOf<Bitmap?>(null)}
 var dst by remember{mutableStateOf<Bitmap?>(null)}
 var tol by remember{mutableFloatStateOf(38f)}
 var color by remember{mutableStateOf("Оригинал")}
 val pick=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){u->if(u!=null)c.contentResolver.openInputStream(u)?.use{src=BitmapFactory.decodeStream(it)}}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
 Text("StampCleaner",style=MaterialTheme.typography.headlineLarge)
 Text("Печать и подпись · прозрачный PNG")
 Button(onClick={pick.launch("image/*")},modifier=Modifier.fillMaxWidth()){Text("Выбрать фото")}
 src?.let{Image(it.asImageBitmap(),"Исходное фото",Modifier.fillMaxWidth().height(180.dp))}
 Text("Чувствительность: ${tol.toInt()}")
 Slider(value=tol,onValueChange={tol=it},valueRange=10f..110f)
 Text("Цвет чернил")
 Row { listOf("Оригинал","Синий","Чёрный","Фиолетовый").forEach{OutlinedButton(onClick={color=it}){Text(it)}} }
 Row { listOf("Красный","Зелёный").forEach{OutlinedButton(onClick={color=it}){Text(it)}} }
 Button(enabled=src!=null,onClick={src?.let{dst=clean(it,tol.toInt(),color)}},modifier=Modifier.fillMaxWidth()){Text("Удалить фон")}
 dst?.let{b->Image(b.asImageBitmap(),"PNG с прозрачностью",Modifier.fillMaxWidth().height(180.dp));Button(onClick={
 val v=ContentValues().apply{put(MediaStore.Images.Media.DISPLAY_NAME,"stamp-cleaner.png");put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(Build.VERSION.SDK_INT>=29)put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/StampCleaner")}
 val u=c.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v)
 val ok=u?.let{c.contentResolver.openOutputStream(it)?.use{s->b.compress(Bitmap.CompressFormat.PNG,100,s)}}?:false
 Toast.makeText(c,if(ok)"PNG сохранён" else "Ошибка сохранения",Toast.LENGTH_LONG).show()
 }){Text("Сохранить PNG")}}
 }
}
private fun clean(b:Bitmap,t:Int,mode:String):Bitmap{
 val w=b.width;val h=b.height;val p=IntArray(w*h);b.getPixels(p,0,w,0,0,w,h)
 val col=when(mode){"Синий"->Color.BLUE;"Чёрный"->Color.BLACK;"Фиолетовый"->Color.rgb(112,35,160);"Красный"->Color.RED;"Зелёный"->Color.rgb(20,120,65);else->0}
 for(i in p.indices){val x=p[i];val l=(.299*Color.red(x)+.587*Color.green(x)+.114*Color.blue(x)).toInt();val a=((255-l-t)*255/(255-t)).coerceIn(0,255);p[i]=if(a<5)Color.TRANSPARENT else if(col==0)Color.argb(a,Color.red(x),Color.green(x),Color.blue(x)) else Color.argb(a,Color.red(col),Color.green(col),Color.blue(col))}
 return Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also{it.setPixels(p,0,w,0,0,w,h)}
}
