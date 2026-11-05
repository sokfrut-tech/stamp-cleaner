const test=require('node:test');
const assert=require('node:assert/strict');
const {processImageData}=require('../processor.js');
function fixture(bg=[248,246,240],w=96,h=96){
 const d=new Uint8ClampedArray(w*h*4);
 for(let y=0;y<h;y++)for(let x=0;x<w;x++){const i=(y*w+x)*4;d[i]=bg[0];d[i+1]=bg[1];d[i+2]=bg[2];d[i+3]=255;}
 // A compact blue signature-like stroke, deliberately occupying less than half of each tile.
 for(let x=24;x<73;x++){const y=Math.round(48+12*Math.sin(x/8));const i=(y*w+x)*4;d[i]=24;d[i+1]=70;d[i+2]=160;}
 return d;
}
for(const [name,bg] of [['white',[255,255,255]],['cream',[246,239,220]],['gray',[220,220,220]]]){
 test(name+' paper is transparent while blue ink remains',()=>{
  const src=fixture(bg),out=processImageData(src,96,96,18,null,false);
  assert.equal(out.data[(0*96+0)*4+3],0,'background corner should be transparent');
  let visibleInk=0;for(let y=32;y<64;y++)for(let x=24;x<73;x++)visibleInk=Math.max(visibleInk,out.data[(y*96+x)*4+3]);assert.ok(visibleInk>0,'signature stroke should remain visible');
  assert.equal(out.width,96);assert.equal(out.height,96);
 });
}
test('recolor changes RGB but preserves extracted alpha',()=>{
 const src=fixture(),a=processImageData(src,96,96,18,null,false),b=processImageData(src,96,96,18,[220,20,40],false);
 assert.deepEqual([...a.data].filter((_,i)=>i%4===3),[...b.data].filter((_,i)=>i%4===3));
 const i=(48*96+48)*4;assert.deepEqual([...b.data.slice(i,i+3)],[220,20,40]);
});
test('automatic crop returns smaller transparent PNG bounds with ink padding',()=>{
 const out=processImageData(fixture(),96,96,18,null,true);
 assert.ok(out.width<96&&out.height<96);
 assert.equal(out.data[3],0,'outer padding remains transparent');
});
test('empty or malformed input is handled safely',()=>{
 const blank=new Uint8ClampedArray(32*32*4);for(let i=3;i<blank.length;i+=4)blank[i]=255;
 const out=processImageData(blank,32,32,18,null,true);assert.equal(out.width,32);assert.equal(out.height,32);
 assert.throws(()=>processImageData(new Uint8ClampedArray(3),2,2,18),/Invalid image data/);
});

test('very pale blue ink retains visible alpha at low sensitivity',()=>{
 const w=96,h=96,d=new Uint8ClampedArray(w*h*4);
 for(let i=0;i<d.length;i+=4){d[i]=248;d[i+1]=248;d[i+2]=246;d[i+3]=255;}
 for(let x=24;x<72;x++){const y=48,i=(y*w+x)*4;d[i]=226;d[i+1]=230;d[i+2]=238;}
 const out=processImageData(d,w,h,8,null,false);
 const alpha=out.data[(48*w+48)*4+3];
 assert.ok(alpha>=20, 'pale blue ink should remain visible');
 assert.equal(out.data[3],0,'paper background should be transparent');
});

test('antialiased blue ink edges are decontaminated instead of retaining paper-colored halos',()=>{
 const w=96,h=96,d=new Uint8ClampedArray(w*h*4),paper=[220,218,212],ink=[18,42,145];
 for(let i=0;i<d.length;i+=4){d[i]=paper[0];d[i+1]=paper[1];d[i+2]=paper[2];d[i+3]=255;}
 // A one-pixel core plus half-covered edge pixels, as produced by a camera or resampling.
 for(let x=24;x<72;x++){
  for(const [y,a] of [[47,.5],[48,1],[49,.5]]){
   const i=(y*w+x)*4;for(let c=0;c<3;c++)d[i+c]=Math.round(paper[c]*(1-a)+ink[c]*a);
  }
 }
 const out=processImageData(d,w,h,8,null,false);
 const edge=(47*w+30)*4,edgeAlpha=out.data[edge+3];
 assert.ok(edgeAlpha>80&&edgeAlpha<255,'antialiased edge should remain partially transparent');
 assert.ok(Math.abs(out.data[edge]-ink[0])<25&&Math.abs(out.data[edge+1]-ink[1])<25&&Math.abs(out.data[edge+2]-ink[2])<25,'edge RGB should be decontaminated toward ink, not paper');
 assert.equal(out.data[3],0,'paper remains transparent');
});
test('paper gradient does not become a visible opaque background',()=>{
 const w=120,h=100,d=new Uint8ClampedArray(w*h*4);
 for(let y=0;y<h;y++)for(let x=0;x<w;x++){const i=(y*w+x)*4,v=245-Math.round(y*.45);d[i]=v;d[i+1]=v-2;d[i+2]=v-5;d[i+3]=255;}
 for(let x=35;x<85;x++){const i=(50*w+x)*4;d[i]=20;d[i+1]=45;d[i+2]=150;}
 const out=processImageData(d,w,h,12,null,false);
 assert.equal(out.data[3],0,'top paper is transparent');
 assert.equal(out.data[((h-1)*w)*4+3],0,'bottom paper is transparent');
 assert.ok(out.data[(50*w+60)*4+3]>200,'blue ink core remains visible');
});
