package com.example.stampcleaner

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as C
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class Ink(val name:String,val color:Int?)
private val inks=listOf(Ink("Оригинал",null),Ink("Синий",C.BLUE),Ink("Чёрный",C.BLACK),Ink("Фиолетовый",0xFF7023A0.toInt()),Ink("Красный",C.RED),Ink("Зелёный",0xFF147841.toInt()))

@Composable fun StampCleanerScreen(){
 val ctx=LocalContext.current;val scope=rememberCoroutineScope()
 var src by remember{mutableStateOf<Bitmap?>(null)};var dst by remember{mutableStateOf<Bitmap?>(null)}
 var tol by remember{mutableFloatStateOf(38f)};var ink by remember{mutableStateOf(inks[0])}
 var status by remember{mutableStateOf("Выберите фото или скан печати.")};var busy by remember{mutableStateOf(false)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->
  if(uri!=null)scope.launch{busy=true;try{src=withContext(Dispatchers.IO){ctx.contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it)}};dst=null;status="Фото загружено."}catch(e:Exception){status="Не удалось открыть изображение."}finally{busy=false}}
 }
 Column(Modifier.fillMaxSize().background(Color(0xFF142720)).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(15.dp)){
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF315747)),contentAlignment=Alignment.Center){Text("🌰",style=MaterialTheme.typography.headlineMedium)}
   Column(Modifier.weight(1f)){Text("StampCleaner",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold,color=Color(0xFFFFD18A));Text("Лесная мастерская PNG",color=Color(0xFFB6CBBF))}
   Text("1.1",color=Color(0xFF9FE3D1))
  }
  Surface(shape=RoundedCornerShape(22.dp),color=Color(0xFF254438)){
   Row(Modifier.fillMaxWidth().padding(17.dp),verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){Text("Спасаем важные чернила!",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,color=Color(0xFFFFE2B5));Text("Убираем бумагу, оставляем подпись и печать — на прозрачном фоне.",color=Color(0xFFD2E1D8))}
    Text("🐿️",style=MaterialTheme.typography.displayMedium)
   }
  }
  Panel("01  Выберите изображение",Icons.Default.Image){
   Button(onClick={picker.launch("image/*")},enabled=!busy,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFFC66D),contentColor=Color(0xFF2A2118))){
    Icon(Icons.Default.Image,null);Spacer(Modifier.width(8.dp));Text(if(src==null)"Выбрать фото или скан" else "Выбрать другое фото",fontWeight=FontWeight.Bold)
   }
   src?.let{Preview(it,"Исходное изображение")}
  }
  Panel("02  Настройте очистку",Icons.Default.Tune){
   Row(verticalAlignment=Alignment.CenterVertically){Text("Чувствительность",Modifier.weight(1f),color=Color(0xFFE8F0EA));Text(tol.toInt().toString(),color=Color(0xFFFFD18A),fontWeight=FontWeight.Bold)}
   Slider(value=tol,onValueChange={tol=it},valueRange=10f..110f,colors=SliderDefaults.colors(thumbColor=Color(0xFFFFC66D),activeTrackColor=Color(0xFF79D6C2)))
   Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Palette,null,tint=Color(0xFF8BDCC9));Spacer(Modifier.width(8.dp));Text("Цвет чернил",color=Color(0xFFE8F0EA),fontWeight=FontWeight.SemiBold)}
   inks.chunked(3).forEach{line->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){line.forEach{choice->
    val active=choice==ink
    OutlinedButton(onClick={ink=choice},modifier=Modifier.weight(1f),shape=RoundedCornerShape(12.dp),contentPadding=PaddingValues(horizontal=2.dp,vertical=8.dp),colors=ButtonDefaults.outlinedButtonColors(containerColor=if(active)Color(0xFF3D6251)else Color.Transparent,contentColor=if(active)Color(0xFFFFD18A)else Color(0xFFE0EAE3))){
     choice.color?.let{Box(Modifier.size(9.dp).clip(CircleShape).background(Color(it)))};if(choice.color!=null)Spacer(Modifier.width(4.dp));Text(choice.name,style=MaterialTheme.typography.labelSmall)
    }
   };repeat(3-line.size){Spacer(Modifier.weight(1f))}}}
   Button(enabled=src!=null&&!busy,onClick={val input=src?:return@Button;scope.launch{busy=true;status="Очищаем фон…";try{dst=withContext(Dispatchers.Default){clean(input,tol.toInt(),ink.color)};status="Готово! Проверьте и сохраните PNG."}catch(e:OutOfMemoryError){status="Фото слишком большое — попробуйте уменьшить размер."}catch(e:Exception){status="Ошибка обработки."}finally{busy=false}}},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF79D6C2),contentColor=Color(0xFF142720))){
    Icon(Icons.Default.AutoFixHigh,null);Spacer(Modifier.width(8.dp));Text(if(busy)"Обрабатываем…" else "Удалить фон",fontWeight=FontWeight.Bold)
   }
  }
  Panel("03  Прозрачный результат",Icons.Default.SaveAlt){
   if(dst==null)Text("Здесь появится ваша печать или подпись.",color=Color(0xFFB6CBBF)) else{
    Preview(dst!!,"PNG без фона")
    Button(onClick={val b=dst?:return@Button;try{val v=ContentValues().apply{put(MediaStore.Images.Media.DISPLAY_NAME,"stamp-cleaner-${System.currentTimeMillis()}.png");put(MediaStore.Images.Media.MIME_TYPE,"image/png");if(Build.VERSION.SDK_INT>=29)put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/StampCleaner")};val u=ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);val ok=u?.let{ctx.contentResolver.openOutputStream(it)?.use{s->b.compress(Bitmap.CompressFormat.PNG,100,s)}}?:false;Toast.makeText(ctx,if(ok)"PNG сохранён в Pictures/StampCleaner" else "Ошибка сохранения",Toast.LENGTH_LONG).show()}catch(e:Exception){Toast.makeText(ctx,"Ошибка сохранения",Toast.LENGTH_LONG).show()}},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFFC66D),contentColor=Color(0xFF2A2118))){Icon(Icons.Default.SaveAlt,null);Spacer(Modifier.width(8.dp));Text("Сохранить PNG",fontWeight=FontWeight.Bold)}
   }
  }
  Text(status,color=Color(0xFFB6CBBF),style=MaterialTheme.typography.bodySmall)
  Text("Совет: равномерный светлый фон помогает сохранить тонкие линии.",color=Color(0xFF8FA99A),style=MaterialTheme.typography.bodySmall)
 }
}
@Composable private fun Panel(title:String,icon:androidx.compose.ui.graphics.vector.ImageVector,content:@Composable ColumnScope.()->Unit){
 Surface(shape=RoundedCornerShape(21.dp),color=Color(0xFF20372F)){Column(Modifier.fillMaxWidth().padding(15.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Color(0xFFFFC66D));Spacer(Modifier.width(9.dp));Text(title,color=Color(0xFFFFE2B5),fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)};content()}}
}
@Composable private fun Preview(bitmap:Bitmap,label:String){
 Box(Modifier.fillMaxWidth().heightIn(max=260.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFE6E9E4)),contentAlignment=Alignment.Center){Image(bitmap.asImageBitmap(),label,Modifier.fillMaxWidth().heightIn(max=260.dp),contentScale=ContentScale.Fit)}
}
private fun clean(b:Bitmap,t:Int,color:Int?):Bitmap{
 val w=b.width;val h=b.height;val p=IntArray(w*h);b.getPixels(p,0,w,0,0,w,h);val threshold=t.coerceIn(1,254)
 for(i in p.indices){val x=p[i];val lum=(.299*C.red(x)+.587*C.green(x)+.114*C.blue(x)).toInt();val dark=255-lum;val a=(((dark-threshold).toFloat()*255)/(255-threshold)).toInt().coerceIn(0,255)*C.alpha(x)/255
 p[i]=if(a<5)C.TRANSPARENT else if(color==null)C.argb(a,C.red(x),C.green(x),C.blue(x))else C.argb(a,C.red(color),C.green(color),C.blue(color))}
 return Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also{it.setPixels(p,0,w,0,0,w,h)}
}
