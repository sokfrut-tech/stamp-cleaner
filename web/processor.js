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
 function estimateInk(data,w,h,paper,threshold){
  const candidates=[];
  for(let y=0;y<h;y+=2)for(let x=0;x<w;x+=2){
   const i=(y*w+x)*4;if(data[i+3]<200)continue;
   const p=paper(x,y),dr=p[0]-data[i],dg=p[1]-data[i+1],db=p[2]-data[i+2],d=Math.hypot(dr,dg,db);
   if(d>Math.max(8,threshold*1.2))candidates.push({d,r:data[i],g:data[i+1],b:data[i+2]});
  }
  if(!candidates.length)return[20,45,130];
  candidates.sort((a,b)=>b.d-a.d);
  const count=Math.min(candidates.length,Math.max(12,Math.ceil(candidates.length*.015)));
  const chosen=candidates.slice(0,count);
  const med=k=>{const a=chosen.map(v=>v[k]).sort((x,y)=>x-y);return a[a.length>>1];};
  return[med('r'),med('g'),med('b')];
 }
 function processImageData(input,w,h,threshold,inkRgb=null,trim=true){
  if(!input||input.length!==w*h*4||w<1||h<1)throw new Error('Invalid image data dimensions');
  const data=new Uint8ClampedArray(input),paper=estimatePaper(data,w,h),limit=Math.max(0,Number(threshold)||0);
  const detected=estimateInk(data,w,h,paper,limit),ink=inkRgb||detected;
  const samples=[];
  for(let y=0;y<h;y+=3)for(let x=0;x<w;x+=3){const i=(y*w+x)*4;if(data[i+3]<200)continue;const p=paper(x,y),v=[p[0]-ink[0],p[1]-ink[1],p[2]-ink[2]],norm=v[0]*v[0]+v[1]*v[1]+v[2]*v[2];if(norm<400)continue;const delta=[p[0]-data[i],p[1]-data[i+1],p[2]-data[i+2]],projection=(delta[0]*v[0]+delta[1]*v[1]+delta[2]*v[2])/Math.sqrt(norm);if(projection>limit+12)samples.push(projection);}
  samples.sort((a,b)=>a-b);const inkDistance=samples.length?samples[Math.floor(samples.length*.9)]:Math.hypot(detected[0]-128,detected[1]-128,detected[2]-128);
  const cutoff=Math.min(.8,limit/Math.max(1,inkDistance)),denom=Math.max(.05,1-cutoff);
  for(let y=0;y<h;y++)for(let x=0;x<w;x++){
   const i=(y*w+x)*4,p=paper(x,y),v=[p[0]-detected[0],p[1]-detected[1],p[2]-detected[2]],norm=v[0]*v[0]+v[1]*v[1]+v[2]*v[2];
   if(norm<400){data[i+3]=0;continue;}
   const delta=[p[0]-data[i],p[1]-data[i+1],p[2]-data[i+2]];
   const raw=(delta[0]*v[0]+delta[1]*v[1]+delta[2]*v[2])/norm;
   const alpha=Math.max(0,Math.min(1,(raw-cutoff)/denom));
   data[i]=ink[0];data[i+1]=ink[1];data[i+2]=ink[2];data[i+3]=alpha<.015?0:Math.round(alpha*255*data[i+3]/255);
  }
  if(!trim)return{data,width:w,height:h};
  let left=w,top=h,right=-1,bottom=-1;
  for(let y=0;y<h;y++)for(let x=0;x<w;x++)if(data[(y*w+x)*4+3]>18){left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}
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
