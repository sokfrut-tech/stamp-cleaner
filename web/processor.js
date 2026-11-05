(function(root){
 'use strict';
 function estimatePaper(data,w,h,tile=24){
  const gw=Math.ceil(w/tile),gh=Math.ceil(h/tile),r=new Float32Array(gw*gh),g=new Float32Array(gw*gh),b=new Float32Array(gw*gh);
  const median=a=>{a.sort((x,y)=>x-y);return a[a.length>>1];};
  for(let gy=0;gy<gh;gy++)for(let gx=0;gx<gw;gx++){
   const rs=[],gs=[],bs=[],step=Math.max(1,Math.floor(tile/8));
   for(let y=gy*tile;y<Math.min(h,(gy+1)*tile);y+=step)for(let x=gx*tile;x<Math.min(w,(gx+1)*tile);x+=step){const i=(y*w+x)*4;rs.push(data[i]);gs.push(data[i+1]);bs.push(data[i+2]);}
   const k=gy*gw+gx;r[k]=median(rs);g[k]=median(gs);b[k]=median(bs);
  }
  function sample(grid,x,y){const fx=Math.max(0,Math.min(gw-1,x/tile-.5)),fy=Math.max(0,Math.min(gh-1,y/tile-.5)),x0=Math.floor(fx),y0=Math.floor(fy),x1=Math.min(gw-1,x0+1),y1=Math.min(gh-1,y0+1),dx=fx-x0,dy=fy-y0;return(grid[y0*gw+x0]*(1-dx)+grid[y0*gw+x1]*dx)*(1-dy)+(grid[y1*gw+x0]*(1-dx)+grid[y1*gw+x1]*dx)*dy;}
  return(x,y)=>[sample(r,x,y),sample(g,x,y),sample(b,x,y)];
 }
 function processImageData(input,w,h,threshold,inkRgb=null,trim=true){
  if(!input||input.length!==w*h*4||w<1||h<1)throw new Error('Invalid image data dimensions');
  const data=new Uint8ClampedArray(input),paper=estimatePaper(data,w,h);
  for(let y=0;y<h;y++)for(let x=0;x<w;x++){
   const i=(y*w+x)*4,[pr,pg,pb]=paper(x,y),dr=pr-data[i],dg=pg-data[i+1],db=pb-data[i+2],distance=Math.sqrt(dr*dr+dg*dg+db*db);
   const alpha=Math.max(0,Math.min(255,Math.round((distance-threshold)*255/70)));data[i+3]=Math.round(alpha*data[i+3]/255);if(alpha<4)data[i+3]=0;
   if(inkRgb){data[i]=inkRgb[0];data[i+1]=inkRgb[1];data[i+2]=inkRgb[2];}
  }
  if(!trim)return{data,width:w,height:h};
  let left=w,top=h,right=-1,bottom=-1;
  for(let y=0;y<h;y++)for(let x=0;x<w;x++)if(data[(y*w+x)*4+3]>8){left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}
  if(right<left||bottom<top)return{data,width:w,height:h};
  const pad=Math.max(3,Math.floor(Math.min(w,h)/100));left=Math.max(0,left-pad);top=Math.max(0,top-pad);right=Math.min(w-1,right+pad);bottom=Math.min(h-1,bottom+pad);
  const width=right-left+1,height=bottom-top+1,out=new Uint8ClampedArray(width*height*4);
  for(let y=0;y<height;y++)out.set(data.subarray(((top+y)*w+left)*4,((top+y)*w+right+1)*4),y*width*4);
  return{data:out,width,height};
 }
 const api={estimatePaper,processImageData};
 if(typeof module!=='undefined'&&module.exports)module.exports=api;
 root.StampProcessor=api;
})(typeof globalThis!=='undefined'?globalThis:this);
