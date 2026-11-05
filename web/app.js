const $=s=>document.querySelector(s);
const file=$('#file'),tol=$('#tol'),value=$('#v'),inkButtons=[...document.querySelectorAll('.swatch')];
const go=$('#go'),save=$('#save'),original=$('#original'),result=$('#canvas');
const octx=original.getContext('2d'),ctx=result.getContext('2d',{willReadFrequently:true});
const trim=$('#trim');const mode=$('#mode');const status=$('#status');let image=null,objectUrl=null,selected='original';
tol.addEventListener('input',()=>value.textContent=tol.value);\nmode.addEventListener('change',()=>{tol.value=mode.value==='signature'?24:mode.value==='text'?48:38;value.textContent=tol.value;});
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
  original.width=result.width=w;original.height=result.height=h;octx.clearRect(0,0,w,h);octx.drawImage(image,0,0,w,h);ctx.clearRect(0,0,w,h);
  original.style.display='block';result.style.display='none';$('#empty-source').style.display='none';$('#empty-result').style.display='block';
  $('#source-info').textContent=f.name+' · '+w+' × '+h+(scale<1?' · уменьшено для обработки на телефоне':'');
  go.disabled=false;save.disabled=true;status.textContent='Изображение загружено. Настройте чувствительность и цвет.';URL.revokeObjectURL(objectUrl);objectUrl=null;
 };
 image.onerror=()=>{status.textContent='Не удалось прочитать это изображение.';go.disabled=true;};image.src=objectUrl;
});
function estimatePaper(data,w,h,tile=24){
 const gw=Math.ceil(w/tile),gh=Math.ceil(h/tile),r=new Float32Array(gw*gh),g=new Float32Array(gw*gh),b=new Float32Array(gw*gh);
 const median=a=>{a.sort((x,y)=>x-y);return a[a.length>>1];};
 for(let gy=0;gy<gh;gy++)for(let gx=0;gx<gw;gx++){
  const rs=[],gs=[],bs=[],step=Math.max(1,Math.floor(tile/8));
  for(let y=gy*tile;y<Math.min(h,(gy+1)*tile);y+=step)for(let x=gx*tile;x<Math.min(w,(gx+1)*tile);x+=step){const i=(y*w+x)*4;rs.push(data[i]);gs.push(data[i+1]);bs.push(data[i+2]);}
  const k=gy*gw+gx;r[k]=median(rs);g[k]=median(gs);b[k]=median(bs);
 }
 function sample(grid,x,y){const fx=Math.max(0,Math.min(gw-1,x/tile-.5)),fy=Math.max(0,Math.min(gh-1,y/tile-.5)),x0=Math.floor(fx),y0=Math.floor(fy),x1=Math.min(gw-1,x0+1),y1=Math.min(gh-1,y0+1),dx=fx-x0,dy=fy-y0;return (grid[y0*gw+x0]*(1-dx)+grid[y0*gw+x1]*dx)*(1-dy)+(grid[y1*gw+x0]*(1-dx)+grid[y1*gw+x1]*dx)*dy;}
 return (x,y)=>[sample(r,x,y),sample(g,x,y),sample(b,x,y)];
}
go.addEventListener('click',()=>{
 if(!image||!original.width)return;go.disabled=true;go.textContent='Обрабатываем…';status.textContent='Оцениваем цвет бумаги и удаляем фон…';
 try{
  const w=original.width,h=original.height,pixels=octx.getImageData(0,0,w,h),data=pixels.data,t=Number(tol.value),paper=estimatePaper(data,w,h);let rgb=null;
  if(selected!=='original'){const n=parseInt(selected.slice(1),16);rgb=[n>>16&255,n>>8&255,n&255];}
  for(let y=0;y<h;y++)for(let x=0;x<w;x++){
   const i=(y*w+x)*4,[pr,pg,pb]=paper(x,y),dr=pr-data[i],dg=pg-data[i+1],db=pb-data[i+2],distance=Math.sqrt(dr*dr+dg*dg+db*db);
   const alpha=Math.max(0,Math.min(255,Math.round((distance-t)*255/(441.7-t))));data[i+3]=Math.round(alpha*data[i+3]/255);if(alpha<4)data[i+3]=0;
   if(rgb){data[i]=rgb[0];data[i+1]=rgb[1];data[i+2]=rgb[2];}
  }
  if(trim.checked){let left=w,top=h,right=-1,bottom=-1;for(let y=0;y<h;y++)for(let x=0;x<w;x++)if(data[(y*w+x)*4+3]>8){if(x<left)left=x;if(x>right)right=x;if(y<top)top=y;if(y>bottom)bottom=y;}if(right>=left&&bottom>=top){const pad=Math.max(3,Math.floor(Math.min(w,h)/100));left=Math.max(0,left-pad);top=Math.max(0,top-pad);right=Math.min(w-1,right+pad);bottom=Math.min(h-1,bottom+pad);const cw=right-left+1,ch=bottom-top+1,cropped=new ImageData(cw,ch);for(let y=0;y<ch;y++){const from=((top+y)*w+left)*4,to=y*cw*4;cropped.data.set(data.subarray(from,from+cw*4),to);}result.width=cw;result.height=ch;ctx.putImageData(cropped,0,0);}else{ctx.putImageData(pixels,0,0);}}else{result.width=w;result.height=h;ctx.putImageData(pixels,0,0);}result.style.display='block';$('#empty-result').style.display='none';save.disabled=false;status.textContent='Готово! Проверьте тонкие линии и скачайте PNG.';
 }catch(err){status.textContent='Не удалось обработать изображение. Попробуйте фото меньшего размера.';}
 finally{go.disabled=false;go.innerHTML='<span>✦</span> Удалить фон';}
});
save.addEventListener('click',()=>{if(!result.width)return;result.toBlob(blob=>{if(!blob){status.textContent='Не удалось создать PNG.';return;}const url=URL.createObjectURL(blob),a=document.createElement('a');a.href=url;a.download='stamp-cleaner.png';document.body.appendChild(a);a.click();a.remove();setTimeout(()=>URL.revokeObjectURL(url),1500);},'image/png');});
window.addEventListener('beforeunload',()=>{if(objectUrl)URL.revokeObjectURL(objectUrl);});
