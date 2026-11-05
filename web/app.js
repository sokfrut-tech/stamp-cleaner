const $=s=>document.querySelector(s);
const file=$('#file'),tol=$('#tol'),value=$('#v'),inkButtons=[...document.querySelectorAll('.swatch')];
const go=$('#go'),save=$('#save'),original=$('#original'),result=$('#canvas');
const octx=original.getContext('2d'),ctx=result.getContext('2d',{willReadFrequently:true});
const status=$('#status');let image=null,objectUrl=null,selected='original';
tol.addEventListener('input',()=>value.textContent=tol.value);
inkButtons.forEach(btn=>btn.addEventListener('click',()=>{inkButtons.forEach(b=>b.classList.remove('active'));btn.classList.add('active');selected=btn.dataset.color;}));
file.addEventListener('change',()=>{
 const f=file.files&&file.files[0];if(!f)return;
 go.disabled=true;save.disabled=true;image=null;original.style.display='none';result.style.display='none';
 $('#empty-source').style.display='block';$('#empty-result').style.display='block';
 if(!f.type.startsWith('image/')){status.textContent='Выберите файл изображения.';return;}
 if(objectUrl)URL.revokeObjectURL(objectUrl);
 objectUrl=URL.createObjectURL(f);image=new Image();
 image.onload=()=>{
  const maxPixels=6250000,scale=Math.min(1,Math.sqrt(maxPixels/(image.naturalWidth*image.naturalHeight)));
  const w=Math.max(1,Math.round(image.naturalWidth*scale)),h=Math.max(1,Math.round(image.naturalHeight*scale));
  original.width=result.width=w;original.height=result.height=h;
  octx.clearRect(0,0,w,h);octx.drawImage(image,0,0,w,h);ctx.clearRect(0,0,w,h);
  original.style.display='block';result.style.display='none';
  $('#empty-source').style.display='none';$('#empty-result').style.display='block';
  $('#source-info').textContent=f.name+' · '+w+' × '+h+(scale<1?' · уменьшено для обработки на телефоне':'');
  go.disabled=false;save.disabled=true;status.textContent='Изображение загружено. Настройте чувствительность и цвет.';
  URL.revokeObjectURL(objectUrl);objectUrl=null;
 };
 image.onerror=()=>{status.textContent='Не удалось прочитать это изображение.';go.disabled=true;};
 image.src=objectUrl;
});
go.addEventListener('click',()=>{
 if(!image||!original.width)return;
 go.disabled=true;go.textContent='Обрабатываем…';status.textContent='Удаляем светлый фон…';
 try{
  const w=original.width,h=original.height;
  const pixels=octx.getImageData(0,0,w,h),data=pixels.data,t=Number(tol.value);
  let rgb=null;
  if(selected!=='original'){const n=parseInt(selected.slice(1),16);rgb=[n>>16&255,n>>8&255,n&255];}
  for(let i=0;i<data.length;i+=4){
   const luminance=.299*data[i]+.587*data[i+1]+.114*data[i+2];
   const darkness=255-luminance;
   const alpha=Math.max(0,Math.min(255,Math.round((darkness-t)*255/(255-t))));
   if(alpha<5)data[i+3]=0;
   else{if(rgb){data[i]=rgb[0];data[i+1]=rgb[1];data[i+2]=rgb[2];}data[i+3]=Math.round(alpha*data[i+3]/255);}
  }
  ctx.putImageData(pixels,0,0);result.style.display='block';$('#empty-result').style.display='none';
  save.disabled=false;status.textContent='Готово! Проверьте тонкие линии и скачайте PNG.';
 }catch(err){status.textContent='Не удалось обработать изображение. Попробуйте фото меньшего размера.';}
 finally{go.disabled=false;go.innerHTML='<span>✦</span> Удалить фон';}
});
save.addEventListener('click',()=>{
 if(!result.width)return;
 result.toBlob(blob=>{
  if(!blob){status.textContent='Не удалось создать PNG.';return;}
  const url=URL.createObjectURL(blob),a=document.createElement('a');
  a.href=url;a.download='stamp-cleaner.png';document.body.appendChild(a);a.click();a.remove();
  setTimeout(()=>URL.revokeObjectURL(url),1500);
 },'image/png');
});
window.addEventListener('beforeunload',()=>{if(objectUrl)URL.revokeObjectURL(objectUrl);});
