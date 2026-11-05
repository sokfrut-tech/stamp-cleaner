package com.example.stampcleaner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as C
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Image as ImageIcon
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

private enum class ContentMode(val title:String,val hint:String){SIGNATURE("Подпись","Сохраняем тонкие рукописные линии"),STAMP("Печать","Сохраняем контур и детали оттиска"),TEXT("Текстовый штамп","Сохраняем мелкие буквы и цифры")}
private data class Ink(val name:String,val color:Int?)
private val inks=listOf(Ink("Оригинал",null),Ink("Синий",C.BLUE),Ink("Чёрный",C.BLACK),Ink("Фиолетовый",0xFF7023A0.toInt()),Ink("Красный",C.RED),Ink("Зелёный",0xFF147841.toInt()))

@Composable fun StampCleanerScreen(){
 val ctx=LocalContext.current;val scope=rememberCoroutineScope()
 var src by remember{mutableStateOf<Bitmap?>(null)};var dst by remember{mutableStateOf<Bitmap?>(null)}
 var tol by remember{mutableFloatStateOf(38f)};var mode by remember{mutableStateOf(ContentMode.STAMP)};var ink by remember{mutableStateOf(inks[0])};var trim by remember{mutableStateOf(true)}
 var status by remember{mutableStateOf("Выберите фото или скан печати.")};var busy by remember{mutableStateOf(false)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->
  if(uri!=null){src=null;dst=null;scope.launch{busy=true;try{val decoded=withContext(Dispatchers.IO){decodeScaled(ctx,uri)};src=decoded;status=if(decoded!=null)"Фото загружено." else "Не удалось прочитать изображение."}catch(e:OutOfMemoryError){status="Фото слишком большое для памяти устройства."}catch(e:Exception){status="Не удалось открыть изображение."}finally{busy=false}}}
 }
 val savePng=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")){uri->
  val bitmap=dst
  if(uri!=null&&bitmap!=null)scope.launch{busy=true;try{val ok=withContext(Dispatchers.IO){ctx.contentResolver.openOutputStream(uri)?.use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}?:false};Toast.makeText(ctx,if(ok)"PNG сохранён." else "Не удалось сохранить PNG.",Toast.LENGTH_LONG).show()}catch(e:Exception){Toast.makeText(ctx,"Ошибка сохранения PNG.",Toast.LENGTH_LONG).show()}finally{busy=false}}
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
  Panel("01  Выберите изображение",Icons.Default.ImageIcon){
   Button(onClick={picker.launch("image/*")},enabled=!busy,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFFC66D),contentColor=Color(0xFF2A2118))){
    Icon(Icons.Default.ImageIcon,null);Spacer(Modifier.width(8.dp));Text(if(src==null)"Выбрать фото или скан" else "Выбрать другое фото",fontWeight=FontWeight.Bold)
   }
   src?.let{Preview(it,"Исходное изображение")}
  }
  Panel("02  Настройте очистку",Icons.Default.Tune){
   Row(verticalAlignment=Alignment.CenterVertically){Text("Чувствительность",Modifier.weight(1f),color=Color(0xFFE8F0EA));Text(tol.toInt().toString(),color=Color(0xFFFFD18A),fontWeight=FontWeight.Bold)}
   Slider(value=tol,onValueChange={tol=it},valueRange=10f..110f,colors=SliderDefaults.colors(thumbColor=Color(0xFFFFC66D),activeTrackColor=Color(0xFF79D6C2)))
   Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=trim,onCheckedChange={trim=it},colors=CheckboxDefaults.colors(checkedColor=Color(0xFF79D6C2)));Column{Text("Обрезать прозрачные поля",color=Color(0xFFE8F0EA),fontWeight=FontWeight.SemiBold);Text("Оставить небольшой отступ вокруг чернил",color=Color(0xFFB6CBBF),style=MaterialTheme.typography.bodySmall)}}
   Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Palette,null,tint=Color(0xFF8BDCC9));Spacer(Modifier.width(8.dp));Text("Цвет чернил",color=Color(0xFFE8F0EA),fontWeight=FontWeight.SemiBold)}
   inks.chunked(3).forEach{line->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){line.forEach{choice->
    val active=choice==ink
    OutlinedButton(onClick={ink=choice},modifier=Modifier.weight(1f),shape=RoundedCornerShape(12.dp),contentPadding=PaddingValues(horizontal=2.dp,vertical=8.dp),colors=ButtonDefaults.outlinedButtonColors(containerColor=if(active)Color(0xFF3D6251)else Color.Transparent,contentColor=if(active)Color(0xFFFFD18A)else Color(0xFFE0EAE3))){
     choice.color?.let{Box(Modifier.size(9.dp).clip(CircleShape).background(Color(it)))};if(choice.color!=null)Spacer(Modifier.width(4.dp));Text(choice.name,style=MaterialTheme.typography.labelSmall)
    }
   };repeat(3-line.size){Spacer(Modifier.weight(1f))}}}
   Button(enabled=src!=null&&!busy,onClick={val input=src?:return@Button;val threshold=tol.toInt();val selectedInk=ink.color;val crop=trim;scope.launch{busy=true;status="Очищаем фон…";try{dst=withContext(Dispatchers.Default){clean(input,threshold,selectedInk,crop)};status="Готово! Проверьте и сохраните PNG."}catch(e:OutOfMemoryError){status="Фото слишком большое — попробуйте уменьшить размер."}catch(e:Exception){status="Ошибка обработки."}finally{busy=false}}},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF79D6C2),contentColor=Color(0xFF142720))){
    Icon(Icons.Default.AutoFixHigh,null);Spacer(Modifier.width(8.dp));Text(if(busy)"Обрабатываем…" else "Удалить фон",fontWeight=FontWeight.Bold)
   }
  }
  Panel("04  Прозрачный результат",Icons.Default.SaveAlt){
   if(dst==null)Text("Здесь появится ваша печать или подпись.",color=Color(0xFFB6CBBF)) else{
    Preview(dst!!,"PNG без фона")
    Button(enabled=!busy,onClick={savePng.launch("stamp-cleaner-${System.currentTimeMillis()}.png")},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFFC66D),contentColor=Color(0xFF2A2118))){Icon(Icons.Default.SaveAlt,null);Spacer(Modifier.width(8.dp));Text("Сохранить PNG",fontWeight=FontWeight.Bold)}
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
 Box(Modifier.fillMaxWidth().heightIn(max=260.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF3F4EF)),contentAlignment=Alignment.Center){
  Canvas(Modifier.matchParentSize()){
   val cell=18.dp.toPx();val light=Color(0xFFF3F4EF);val dark=Color(0xFFD8DDD5)
   val cols=(size.width/cell).toInt()+1;val rows=(size.height/cell).toInt()+1
   for(y in 0 until rows)for(x in 0 until cols){
    drawRect(if((x+y)%2==0)light else dark,topLeft=androidx.compose.ui.geometry.Offset(x*cell,y*cell),size=androidx.compose.ui.geometry.Size(cell,cell))
   }
  }
  Image(bitmap.asImageBitmap(),label,Modifier.fillMaxWidth().heightIn(max=260.dp),contentScale=ContentScale.Fit)
 }
}
private fun clean(b:Bitmap,t:Int,color:Int?,trim:Boolean):Bitmap {
 val w=b.width;val h=b.height;val pixels=IntArray(w*h);b.getPixels(pixels,0,w,0,0,w,h)
 val tile=24;val gw=(w+tile-1)/tile;val gh=(h+tile-1)/tile
 val br=IntArray(gw*gh);val bg=IntArray(gw*gh);val bb=IntArray(gw*gh)
 fun med(v:MutableList<Int>):Int{v.sort();return v[v.size/2]}
 for(gy in 0 until gh)for(gx in 0 until gw){
  val rs=ArrayList<Int>();val gs=ArrayList<Int>();val bs=ArrayList<Int>();val step=maxOf(1,tile/8)
  for(y in gy*tile until minOf(h,(gy+1)*tile) step step)for(x in gx*tile until minOf(w,(gx+1)*tile) step step){
   val c=pixels[y*w+x];rs.add(C.red(c));gs.add(C.green(c));bs.add(C.blue(c))
  }
  val k=gy*gw+gx;br[k]=med(rs);bg[k]=med(gs);bb[k]=med(bs)
 }
 fun sample(g:IntArray,x:Int,y:Int):Float{
  val fx=(x.toFloat()/tile-.5f).coerceIn(0f,(gw-1).toFloat());val fy=(y.toFloat()/tile-.5f).coerceIn(0f,(gh-1).toFloat())
  val x0=fx.toInt();val y0=fy.toInt();val x1=minOf(gw-1,x0+1);val y1=minOf(gh-1,y0+1);val dx=fx-x0;val dy=fy-y0
  val top=g[y0*gw+x0]*(1-dx)+g[y0*gw+x1]*dx;val bottom=g[y1*gw+x0]*(1-dx)+g[y1*gw+x1]*dx
  return top*(1-dy)+bottom*dy
 }
 val threshold=t.coerceIn(5,110).toFloat()
 for(y in 0 until h)for(x in 0 until w){
  val i=y*w+x;val c=pixels[i]
  val dr=sample(br,x,y)-C.red(c);val dg=sample(bg,x,y)-C.green(c);val db=sample(bb,x,y)-C.blue(c)
  val distance=kotlin.math.sqrt(dr*dr+dg*dg+db*db)
  val alpha=(((distance-threshold)/(441.7f-threshold))*255f).toInt().coerceIn(0,255)*C.alpha(c)/255
  pixels[i]=if(alpha<4)C.TRANSPARENT else if(color==null)C.argb(alpha,C.red(c),C.green(c),C.blue(c))else C.argb(alpha,C.red(color),C.green(color),C.blue(color))
 }
 val output=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also{it.setPixels(pixels,0,w,0,0,w,h)}
 if(!trim)return output
 var left=w;var top=h;var right=-1;var bottom=-1
 for(y in 0 until h)for(x in 0 until w)if(C.alpha(pixels[y*w+x])>8){if(x<left)left=x;if(x>right)right=x;if(y<top)top=y;if(y>bottom)bottom=y}
 if(right<left||bottom<top)return output
 val pad=maxOf(3,minOf(w,h)/100);left=(left-pad).coerceAtLeast(0);top=(top-pad).coerceAtLeast(0);right=(right+pad).coerceAtMost(w-1);bottom=(bottom+pad).coerceAtMost(h-1)
 val cropped=Bitmap.createBitmap(output,left,top,right-left+1,bottom-top+1);if(cropped!==output)output.recycle();return cropped
}

private fun decodeScaled(context:android.content.Context,uri:android.net.Uri):Bitmap?{
 val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
 context.contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,bounds)}
 if(bounds.outWidth<=0||bounds.outHeight<=0)return null
 var sample=1
 while(maxOf(bounds.outWidth/sample,bounds.outHeight/sample)>2500)sample*=2
 val options=BitmapFactory.Options().apply{inSampleSize=sample;inPreferredConfig=Bitmap.Config.ARGB_8888}
 return context.contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,options)}
}
