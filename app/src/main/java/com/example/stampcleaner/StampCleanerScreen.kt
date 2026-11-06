package com.example.stampcleaner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as C
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32

private enum class ContentMode(val title:String,val hint:String){SIGNATURE("Подпись","Сохраняем тонкие рукописные линии"),STAMP("Печать","Сохраняем контур и детали оттиска"),TEXT("Текстовый штамп","Сохраняем мелкие буквы и цифры")}
private data class Ink(val name:String,val color:Int?)
private val inks=listOf(Ink("Оригинал",null),Ink("Синий",C.BLUE),Ink("Чёрный",C.BLACK),Ink("Фиолетовый",0xFF7023A0.toInt()),Ink("Красный",C.RED),Ink("Зелёный",0xFF147841.toInt()))

@Composable fun StampCleanerScreen(){
 val ctx=LocalContext.current;val scope=rememberCoroutineScope()
 var src by remember{mutableStateOf<Bitmap?>(null)};var dst by remember{mutableStateOf<Bitmap?>(null)}
 var widthMm by remember{mutableStateOf("40")};var heightMm by remember{mutableStateOf("40")};var dpi by remember{mutableIntStateOf(300)}
 var tol by remember{mutableFloatStateOf(12f)};var blueHueTolerance by remember{mutableFloatStateOf(40f)};var mode by remember{mutableStateOf(ContentMode.STAMP)};var ink by remember{mutableStateOf(inks[0])};var trim by remember{mutableStateOf(true)};var inkOnly by remember{mutableStateOf(false)};var blueHsvOnly by remember{mutableStateOf(false)}
 var status by remember{mutableStateOf("Выберите фото или скан печати.")};var busy by remember{mutableStateOf(false)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->
  if(uri!=null){src=null;dst=null;scope.launch{busy=true;try{val decoded=withContext(Dispatchers.IO){decodeScaled(ctx,uri)};src=decoded;status=if(decoded!=null)"Фото загружено." else "Не удалось прочитать изображение."}catch(e:OutOfMemoryError){status="Фото слишком большое для памяти устройства."}catch(e:Exception){status="Не удалось открыть изображение."}finally{busy=false}}}
 }
 val savePng=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")){uri->
  val bitmap=dst
  if(uri!=null&&bitmap!=null)scope.launch{busy=true;try{val ok=withContext(Dispatchers.IO){ctx.contentResolver.openOutputStream(uri)?.use{out->out.write(exportPng(bitmap,widthMm.toFloat(),heightMm.toFloat(),dpi));true}?:false};Toast.makeText(ctx,if(ok)"PNG сохранён." else "Не удалось сохранить PNG.",Toast.LENGTH_LONG).show()}catch(e:Exception){Toast.makeText(ctx,"Ошибка сохранения PNG.",Toast.LENGTH_LONG).show()}finally{busy=false}}
 }
 Column(Modifier.fillMaxSize().padding(top = 58.dp).background(Color.Transparent).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(15.dp)){
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF52235D)),contentAlignment=Alignment.Center){Text("✦",style=MaterialTheme.typography.headlineMedium)}
   Column(Modifier.weight(1f)){Text("StampCleaner",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold,color=Color(0xFFFFB5E8));Text("NEON CLUB · STAMP STUDIO",color=Color(0xFFD2B6D4))}
   Text("1.1",color=Color(0xFF9FE3D1))
  }
  Surface(shape=RoundedCornerShape(22.dp),color=Color(0xFF32183D)){
   Row(Modifier.fillMaxWidth().padding(17.dp),verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){Text("Твои чернила. Твой стиль.",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,color=Color(0xFFFFD7F2));Text("Убираем бумагу, оставляем подпись и печать — на прозрачном фоне.",color=Color(0xFFD2E1D8))}
    Text("✧",style=MaterialTheme.typography.displayMedium)
   }
  }
  Panel("01  Выберите изображение",Icons.Default.ImageIcon){
   Button(onClick={picker.launch("image/*")},enabled=!busy,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFF9DDF),contentColor=Color(0xFF210D2B))){
    Icon(Icons.Default.ImageIcon,null);Spacer(Modifier.width(8.dp));Text(if(src==null)"Выбрать фото или скан" else "Выбрать другое фото",fontWeight=FontWeight.Bold)
   }
   src?.let{Preview(it,"Исходное изображение")}
  }
  Panel("02  Настройте очистку",Icons.Default.Tune){
   Row(verticalAlignment=Alignment.CenterVertically){Text("Чувствительность",Modifier.weight(1f),color=Color(0xFFFFEAF8));Text(tol.toInt().toString(),color=Color(0xFFFFB5E8),fontWeight=FontWeight.Bold)}
   Slider(value=tol,onValueChange={tol=it},valueRange=5f..110f,colors=SliderDefaults.colors(thumbColor=Color(0xFFFF9DDF),activeTrackColor=Color(0xFFD28BFF)))
   Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=trim,onCheckedChange={trim=it},colors=CheckboxDefaults.colors(checkedColor=Color(0xFFD28BFF)));Column{Text("Обрезать прозрачные поля",color=Color(0xFFFFEAF8),fontWeight=FontWeight.SemiBold);Text("Оставить небольшой отступ вокруг чернил",color=Color(0xFFD2B6D4),style=MaterialTheme.typography.bodySmall)}}
   Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=inkOnly,onCheckedChange={inkOnly=it},colors=CheckboxDefaults.colors(checkedColor=Color(0xFFD28BFF)));Column{Text("Только цвет чернил",color=Color(0xFFFFEAF8),fontWeight=FontWeight.SemiBold);Text("Подавляет серый и чёрный текст рядом с цветной печатью",color=Color(0xFFD2B6D4),style=MaterialTheme.typography.bodySmall)}}
   Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=blueHsvOnly,onCheckedChange={blueHsvOnly=it},colors=CheckboxDefaults.colors(checkedColor=Color(0xFFD28BFF)));Column{Text("Выделять синие чернила (HSV)",color=Color(0xFFFFEAF8),fontWeight=FontWeight.SemiBold);Text("Дополнительный режим по цветовому диапазону; полезен для синей печати на фоне чёрного текста.",color=Color(0xFFD2B6D4),style=MaterialTheme.typography.bodySmall)}}
   if(blueHsvOnly){Row(verticalAlignment=Alignment.CenterVertically){Text("Ширина синего диапазона",Modifier.weight(1f),color=Color(0xFFFFEAF8));Text("±${blueHueTolerance.toInt()}°",color=Color(0xFFFFB5E8),fontWeight=FontWeight.Bold)};Slider(value=blueHueTolerance,onValueChange={blueHueTolerance=it},valueRange=15f..70f,colors=SliderDefaults.colors(thumbColor=Color(0xFFFF9DDF),activeTrackColor=Color(0xFFD28BFF)));Text("Увеличьте диапазон, если синие чернила уходят в голубой или фиолетовый.",color=Color(0xFFD2B6D4),style=MaterialTheme.typography.bodySmall)}
   Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Palette,null,tint=Color(0xFFE49BFF));Spacer(Modifier.width(8.dp));Text("Цвет чернил",color=Color(0xFFFFEAF8),fontWeight=FontWeight.SemiBold)}
   inks.chunked(3).forEach{line->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){line.forEach{choice->
    val active=choice==ink
    OutlinedButton(onClick={ink=choice},modifier=Modifier.weight(1f),shape=RoundedCornerShape(12.dp),contentPadding=PaddingValues(horizontal=2.dp,vertical=8.dp),colors=ButtonDefaults.outlinedButtonColors(containerColor=if(active)Color(0xFF3D6251)else Color.Transparent,contentColor=if(active)Color(0xFFFFB5E8)else Color(0xFFF4DFF3))){
     choice.color?.let{Box(Modifier.size(9.dp).clip(CircleShape).background(Color(it)))};if(choice.color!=null)Spacer(Modifier.width(4.dp));Text(choice.name,style=MaterialTheme.typography.labelSmall)
    }
   };repeat(3-line.size){Spacer(Modifier.weight(1f))}}}
   Button(enabled=src!=null&&!busy,onClick={val input=src?:return@Button;val threshold=tol.toInt();val selectedInk=ink.color;val crop=trim;val filterInk=inkOnly;val filterBlue=blueHsvOnly;val hueTolerance=blueHueTolerance;scope.launch{busy=true;status="Очищаем фон…";try{dst=withContext(Dispatchers.Default){clean(input,threshold,selectedInk,crop,filterInk,filterBlue,hueTolerance)};status="Готово! Проверьте и сохраните PNG."}catch(e:OutOfMemoryError){status="Фото слишком большое — попробуйте уменьшить размер."}catch(e:Exception){status="Ошибка обработки."}finally{busy=false}}},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFD28BFF),contentColor=Color(0xFF0B0711))){
    Icon(Icons.Default.AutoFixHigh,null);Spacer(Modifier.width(8.dp));Text(if(busy)"Обрабатываем…" else "Удалить фон",fontWeight=FontWeight.Bold)
   }
  }
  Panel("04  Прозрачный результат",Icons.Default.SaveAlt){
   if(dst==null)Text("Здесь появится ваша печать или подпись.",color=Color(0xFFD2B6D4)) else{
    CropPreview(dst!!){cropped->dst=cropped;status="Ручная обрезка применена."}
    Text("Физический размер при экспорте",color=Color(0xFFFFD7F2),fontWeight=FontWeight.Bold)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(value=widthMm,onValueChange={widthMm=it},label={Text("Ширина, мм")},modifier=Modifier.weight(1f),singleLine=true);OutlinedTextField(value=heightMm,onValueChange={heightMm=it},label={Text("Высота, мм")},modifier=Modifier.weight(1f),singleLine=true)}
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(150,300,600).forEach{value->FilterChip(selected=dpi==value,onClick={dpi=value},label={Text("$value DPI")})}}
    Text("PNG получит заданный физический холст и DPI. Подпись не увеличивается искусственно — так сохраняются исходные пиксели.",color=Color(0xFFD2B6D4),style=MaterialTheme.typography.bodySmall)
    Button(enabled=!busy,onClick={savePng.launch("stamp-cleaner-${System.currentTimeMillis()}.png")},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFF9DDF),contentColor=Color(0xFF210D2B))){Icon(Icons.Default.SaveAlt,null);Spacer(Modifier.width(8.dp));Text("Сохранить PNG",fontWeight=FontWeight.Bold)}
   }
  }
  Text(status,color=Color(0xFFD2B6D4),style=MaterialTheme.typography.bodySmall)
  Text("Совет: равномерный светлый фон помогает сохранить тонкие линии.",color=Color(0xFF8FA99A),style=MaterialTheme.typography.bodySmall)
 }
}
@Composable private fun Panel(title:String,icon:androidx.compose.ui.graphics.vector.ImageVector,content:@Composable ColumnScope.()->Unit){
 Surface(shape=RoundedCornerShape(21.dp),color=Color(0xFF24132E)){Column(Modifier.fillMaxWidth().padding(15.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Color(0xFFFF9DDF));Spacer(Modifier.width(9.dp));Text(title,color=Color(0xFFFFD7F2),fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)};content()}}
}
@Composable private fun Preview(bitmap:Bitmap,label:String){
 Box(Modifier.fillMaxWidth().heightIn(max=260.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF28172E)),contentAlignment=Alignment.Center){
  Canvas(Modifier.matchParentSize()){
   val cell=18.dp.toPx();val light=Color(0xFF28172E);val dark=Color(0xFF4A3152)
   val cols=(size.width/cell).toInt()+1;val rows=(size.height/cell).toInt()+1
   for(y in 0 until rows)for(x in 0 until cols){
    drawRect(if((x+y)%2==0)light else dark,topLeft=androidx.compose.ui.geometry.Offset(x*cell,y*cell),size=androidx.compose.ui.geometry.Size(cell,cell))
   }
  }
  Image(bitmap.asImageBitmap(),label,Modifier.fillMaxWidth().heightIn(max=260.dp),contentScale=ContentScale.Fit)
 }
}
private fun clean(b:Bitmap,t:Int,color:Int?,trim:Boolean,inkOnly:Boolean=false,blueHsvOnly:Boolean=false,blueHueTolerance:Float=40f):Bitmap {
 val w=b.width;val h=b.height;val pixels=IntArray(w*h);b.getPixels(pixels,0,w,0,0,w,h)
 val tile=24;val gw=(w+tile-1)/tile;val gh=(h+tile-1)/tile
 val br=IntArray(gw*gh);val bg=IntArray(gw*gh);val bb=IntArray(gw*gh)
 fun median(v:MutableList<Int>):Int{v.sort();return v[v.size/2]}
 for(gy in 0 until gh)for(gx in 0 until gw){
  val rs=ArrayList<Int>();val gs=ArrayList<Int>();val bs=ArrayList<Int>();val step=maxOf(1,tile/8)
  for(y in gy*tile until minOf(h,(gy+1)*tile) step step)for(x in gx*tile until minOf(w,(gx+1)*tile) step step){
   val c=pixels[y*w+x];rs.add(C.red(c));gs.add(C.green(c));bs.add(C.blue(c))
  }
  val k=gy*gw+gx;br[k]=median(rs);bg[k]=median(gs);bb[k]=median(bs)
 }
 fun paper(x:Int,y:Int):IntArray{
  val fx=(x.toFloat()/tile-.5f).coerceIn(0f,(gw-1).toFloat());val fy=(y.toFloat()/tile-.5f).coerceIn(0f,(gh-1).toFloat())
  val x0=fx.toInt();val y0=fy.toInt();val x1=minOf(gw-1,x0+1);val y1=minOf(gh-1,y0+1);val dx=fx-x0;val dy=fy-y0
  fun sample(g:IntArray):Int{val top=g[y0*gw+x0]*(1-dx)+g[y0*gw+x1]*dx;val bottom=g[y1*gw+x0]*(1-dx)+g[y1*gw+x1]*dx;return (top*(1-dy)+bottom*dy).toInt()}
  return intArrayOf(sample(br),sample(bg),sample(bb))
 }
 val threshold=t.coerceIn(5,110).toFloat()
 val capacity=((w+3)/4)*((h+3)/4);val candidates=LongArray(capacity);var count=0
 for(y in 0 until h step 4)for(x in 0 until w step 4){
  val c=pixels[y*w+x];val p=paper(x,y);val dr=p[0]-C.red(c);val dg=p[1]-C.green(c);val db=p[2]-C.blue(c)
  val distance=kotlin.math.sqrt((dr*dr+dg*dg+db*db).toFloat())
  if(distance>threshold+24){val score=(distance*1000).toLong().coerceAtMost(0x7fffffff);val rgb=(C.red(c) shl 16) or (C.green(c) shl 8) or C.blue(c);candidates[count++]=(score shl 24) or rgb.toLong()}
 }
 val detected=if(count==0)intArrayOf(20,45,130) else{
  java.util.Arrays.sort(candidates,0,count);val take=minOf(count,maxOf(12,(count*.015f).toInt()+1));val from=count-take
  fun channel(shift:Int):Int{val values=IntArray(take){((candidates[from+it] and 0xFFFFFFL).toInt() shr shift) and 255};values.sort();return values[take/2]}
  intArrayOf(channel(16),channel(8),channel(0))
 }
 val ink=color?.let{intArrayOf(C.red(it),C.green(it),C.blue(it))}?:detected
 val vr=(br[0].toFloat()-ink[0]);val vg=(bg[0].toFloat()-ink[1]);val vb=(bb[0].toFloat()-ink[2])
 // The local paper estimate supplies the per-pixel projection; use the median paper-to-ink distance for a stable threshold.
 val distances=FloatArray(gw*gh){i->kotlin.math.sqrt(((br[i]-ink[0]).toFloat().let{it*it})+((bg[i]-ink[1]).toFloat().let{it*it})+((bb[i]-ink[2]).toFloat().let{it*it}))}
 distances.sort();val inkDistance=distances[distances.size/2].coerceAtLeast(1f)
 val cutoff=(threshold/inkDistance).coerceIn(0f,.8f);val denom=(1f-cutoff).coerceAtLeast(.05f)
 for(y in 0 until h)for(x in 0 until w){
  val i=y*w+x;val c=pixels[i];val p=paper(x,y);val r=p[0]-ink[0];val g=p[1]-ink[1];val bl=p[2]-ink[2]
  val norm=(r*r+g*g+bl*bl).toFloat()
  if(norm<400f){pixels[i]=C.TRANSPARENT;continue}
  val dr=p[0]-C.red(c);val dg=p[1]-C.green(c);val db=p[2]-C.blue(c)
  val raw=(dr*r+dg*g+db*bl)/norm
  val linearAlpha=((raw-cutoff)/denom).coerceIn(0f,1f)
  var alpha=Math.pow(linearAlpha.toDouble(),1.12).toFloat()
  if(blueHsvOnly){
   // OpenCV hue 90..130 maps to Android HSV 180..260 degrees.
   // User-adjustable width expands/contracts the blue band around its 220-degree center.
   // Smooth edges avoid hard cut-outs in antialiased stamp contours.
   val hsv=FloatArray(3);C.RGBToHSV(C.red(c),C.green(c),C.blue(c),hsv)
   val half=blueHueTolerance.coerceIn(15f,70f)
   val delta=kotlin.math.abs(((hsv[0]-220f+540f)%360f)-180f)
   val hueKeep=((half+6f-delta)/12f).coerceIn(0f,1f)
   val satKeep=((hsv[1]-.18f)/.30f).coerceIn(0f,1f)
   alpha*=hueKeep*satKeep
  }
  if(inkOnly){
   // Keep hues close to the detected/selected ink while suppressing unrelated grey/black form text.
   // Soft transition protects antialiased stamp edges and natural ink-density variation.
   val cr=C.red(c)-ink[0];val cg=C.green(c)-ink[1];val cb=C.blue(c)-ink[2]
   val hueDistance=kotlin.math.sqrt((cr*cr+cg*cg+cb*cb).toFloat())
   val keep=((150f-hueDistance)/75f).coerceIn(0f,1f)
   alpha*=keep
  }
  // Drop faint paper residue (typically 1–20/255 alpha) that can show as a gray/black matte in office apps.
  // Keep antialiased stamp edges above this floor so fine ring text and strokes remain intact.
  if(alpha<.08f)pixels[i]=C.TRANSPARENT else if(color!=null)pixels[i]=C.argb((alpha*255).toInt(),ink[0],ink[1],ink[2]) else {
   // Original mode preserves the photographed ink hue and removes paper contamination at soft edges.
   val a=alpha.coerceAtLeast(.08f)
   val rr=(p[0]+(C.red(c)-p[0])/a).toInt().coerceIn(0,255)
   val gg=(p[1]+(C.green(c)-p[1])/a).toInt().coerceIn(0,255)
   val blc=(p[2]+(C.blue(c)-p[2])/a).toInt().coerceIn(0,255)
   pixels[i]=C.argb((alpha*255).toInt(),rr,gg,blc)
  }
 }
 // Remove only isolated 1–3 pixel dust specks. Small stamp glyphs and punctuation are larger
 // or connected to neighboring ink; keep every component of four pixels or more.
 val visited=BooleanArray(w*h)
 val queue=IntArray(w*h)
 for(start in pixels.indices){
  if(visited[start]||C.alpha(pixels[start])<=18)continue
  var head=0;var tail=0
  queue[tail++]=start;visited[start]=true
  while(head<tail){
   val at=queue[head++];val x=at%w;val y=at/w
   for(dy in -1..1)for(dx in -1..1){
    if(dx==0&&dy==0)continue
    val nx=x+dx;val ny=y+dy
    if(nx !in 0 until w||ny !in 0 until h)continue
    val ni=ny*w+nx
    if(!visited[ni]&&C.alpha(pixels[ni])>18){visited[ni]=true;queue[tail++]=ni}
   }
  }
  if(tail<=3)for(k in 0 until tail)pixels[queue[k]]=C.TRANSPARENT
 }
 val output=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also{it.setPixels(pixels,0,w,0,0,w,h)}
 if(!trim)return output
 var left=w;var top=h;var right=-1;var bottom=-1
 for(y in 0 until h)for(x in 0 until w)if(C.alpha(pixels[y*w+x])>18){if(x<left)left=x;if(x>right)right=x;if(y<top)top=y;if(y>bottom)bottom=y}
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

private fun exportPng(source:Bitmap,widthMm:Float,heightMm:Float,dpi:Int):ByteArray{
 require(widthMm in 5f..300f&&heightMm in 5f..300f){"Размер должен быть от 5 до 300 мм"}
 val w=(widthMm*dpi/25.4f).toInt().coerceAtLeast(1);val h=(heightMm*dpi/25.4f).toInt().coerceAtLeast(1)
 require(w.toLong()*h<=40_000_000L){"Слишком большое изображение"}
 val target=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
 val scale=minOf(1f,w.toFloat()/source.width,h.toFloat()/source.height);val sw=source.width*scale;val sh=source.height*scale
 // Never enlarge a signature during export: upscaling invents interpolated pixels and visibly softens fine strokes.
 val paint=android.graphics.Paint().apply{isFilterBitmap=false;isAntiAlias=false}
 android.graphics.Canvas(target).drawBitmap(source,null,android.graphics.RectF((w-sw)/2f,(h-sh)/2f,(w+sw)/2f,(h+sh)/2f),paint)
 val raw=ByteArrayOutputStream();target.compress(Bitmap.CompressFormat.PNG,100,raw);target.recycle()
 val png=raw.toByteArray();val ppm=(dpi/0.0254).toLong()
 val chunkData=ByteArrayOutputStream().apply{write(byteArrayOf(0x70,0x48,0x59,0x73));write(byteArrayOf((ppm shr 24).toByte(),(ppm shr 16).toByte(),(ppm shr 8).toByte(),ppm.toByte(),(ppm shr 24).toByte(),(ppm shr 16).toByte(),(ppm shr 8).toByte(),ppm.toByte(),1))}.toByteArray()
 val crc=CRC32().apply{update(chunkData)}.value
 val chunk=ByteArrayOutputStream().apply{write(byteArrayOf(0,0,0,9));write(chunkData);write(byteArrayOf((crc shr 24).toByte(),(crc shr 16).toByte(),(crc shr 8).toByte(),crc.toByte()))}.toByteArray()
 return ByteArrayOutputStream().apply{write(png,0,33);write(chunk);write(png,33,png.size-33)}.toByteArray()
}

@Composable private fun CropPreview(bitmap:Bitmap,onCrop:(Bitmap)->Unit){
 var start by remember(bitmap){mutableStateOf<Offset?>(null)}
 var end by remember(bitmap){mutableStateOf<Offset?>(null)}
 var selection by remember(bitmap){mutableStateOf<Rect?>(null)}
 var gestureMode by remember{mutableStateOf("draw")}
 var gestureStart by remember{mutableStateOf(Offset.Zero)}
 var initialSelection by remember{mutableStateOf<Rect?>(null)}
 var canvasWidth by remember{mutableFloatStateOf(0f)}
 val canvasHeightPx=with(LocalDensity.current){160.dp.toPx()}
 Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
  Text("Ручная обрезка",color=Color(0xFFFFD7F2),fontWeight=FontWeight.Bold)
  Text("Проведите пальцем по изображению, чтобы выделить нужную область.",color=Color(0xFFD2B6D4),style=MaterialTheme.typography.bodySmall)
  Canvas(Modifier.fillMaxWidth().height(160.dp).onSizeChanged{canvasWidth=it.width.toFloat()}.pointerInput(bitmap){detectDragGestures(onDragStart={p->gestureStart=p;initialSelection=selection;val r=selection;gestureMode=when{r==null->"draw";kotlin.math.hypot(p.x-r.right,p.y-r.bottom)<34f->"resize";p.x in r.left..r.right&&p.y in r.top..r.bottom->"move";else->"draw"};if(gestureMode=="draw"){start=p;end=p;selection=null}},onDragEnd={val a=start;val b=end;if(gestureMode=="draw"&&a!=null&&b!=null)selection=Rect(minOf(a.x,b.x),minOf(a.y,b.y),maxOf(a.x,b.x),maxOf(a.y,b.y))},onDragCancel={start=null;end=null}){change,_->val p=change.position;val r=initialSelection;when(gestureMode){"draw"->end=p;"move"->{if(r!=null){val dx=p.x-gestureStart.x;val dy=p.y-gestureStart.y;val w=r.width;val h=r.height;val left=(r.left+dx).coerceIn(0f,size.width-w);val top=(r.top+dy).coerceIn(0f,size.height-h);selection=Rect(left,top,left+w,top+h)}};"resize"->{if(r!=null){val right:Float=p.x.coerceAtLeast(r.left+8f).coerceAtMost(size.width.toFloat());val bottom:Float=p.y.coerceAtLeast(r.top+8f).coerceAtMost(size.height.toFloat());selection=Rect(r.left,r.top,right,bottom)}}};change.consume()}}){
   val scale=minOf(size.width/bitmap.width,size.height/bitmap.height);val dw=bitmap.width*scale;val dh=bitmap.height*scale;val ox=(size.width-dw)/2f;val oy=(size.height-dh)/2f
   val cell=18.dp.toPx();val light=Color(0xFF28172E);val dark=Color(0xFF4A3152)
   val cols=(size.width/cell).toInt()+1;val rows=(size.height/cell).toInt()+1
   for(y in 0 until rows)for(x in 0 until cols){drawRect(if((x+y)%2==0)light else dark,topLeft=Offset(x*cell,y*cell),size=androidx.compose.ui.geometry.Size(cell,cell))}
   drawImage(bitmap.asImageBitmap(),dstOffset=androidx.compose.ui.unit.IntOffset(ox.toInt(),oy.toInt()),dstSize=androidx.compose.ui.unit.IntSize(dw.toInt(),dh.toInt()))
   val r=selection?:if(start!=null&&end!=null)Rect(minOf(start!!.x,end!!.x),minOf(start!!.y,end!!.y),maxOf(start!!.x,end!!.x),maxOf(start!!.y,end!!.y))else null
   r?.let{drawRect(Color(0xFFFF9DDF),topLeft=Offset(it.left,it.top),size=androidx.compose.ui.geometry.Size(it.width,it.height),style=Stroke(3.dp.toPx()));drawCircle(Color(0xFFFF9DDF),radius=7.dp.toPx(),center=Offset(it.right,it.bottom))}
  }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   Button(enabled=selection!=null&&selection!!.width>8&&selection!!.height>8,onClick={val r=selection?:return@Button;val scale=minOf(canvasWidth/bitmap.width,canvasHeightPx);val viewW=bitmap.width*scale;val viewH=bitmap.height*scale;val ox=(canvasWidth-viewW)/2f;val oy=(canvasHeightPx-viewH)/2f;val l=((r.left-ox)/scale).toInt().coerceIn(0,bitmap.width-1);val t=((r.top-oy)/scale).toInt().coerceIn(0,bitmap.height-1);val rr=((r.right-ox)/scale).toInt().coerceIn(l+1,bitmap.width);val bb=((r.bottom-oy)/scale).toInt().coerceIn(t+1,bitmap.height);onCrop(Bitmap.createBitmap(bitmap,l,t,rr-l,bb-t))},modifier=Modifier.weight(1f).height(44.dp),contentPadding=PaddingValues(horizontal=6.dp,vertical=4.dp)){Text("Обрезать",maxLines=1,style=MaterialTheme.typography.labelLarge)}
   OutlinedButton(enabled=selection!=null,onClick={selection=null;start=null;end=null},modifier=Modifier.weight(1f).height(44.dp),contentPadding=PaddingValues(horizontal=6.dp,vertical=4.dp)){Text("Сбросить рамку",maxLines=1,style=MaterialTheme.typography.labelLarge)}
  }
 }
}
