const canvas = document.querySelector('#codex');
const ctx = canvas.getContext('2d');
const poem = document.querySelector('#poem');
const seed = document.querySelector('#seed');
const translation = document.querySelector('#translation');
const grammar = document.querySelector('#grammar');

const PALETTE = [
    'rgb(155, 20, 30)',  // Deep red
    'rgb(20, 30, 180)',  // Cobalt Blue
    'rgb(30, 120, 50)',  // Forest Green
    'rgb(200, 130, 50)', // Warm Ochre
    'rgb(200, 20, 100)', // Magenta
    'rgb(50, 140, 150)', // Teal
    'rgb(110, 30, 150)'  // Purple
];

function hash(text) { let h=2166136261; for (const c of text) { h^=c.charCodeAt(0); h=Math.imul(h,16777619); } return Math.abs(h>>>0); }
function rng(value) { let a=value>>>0; return () => { a|=0;a=a+0x6D2B79F5|0;let t=Math.imul(a^a>>>15,1|a);t=t+Math.imul(t^t>>>7,61|t)^t;return ((t^t>>>14)>>>0)/4294967296; }; }

function drawCursiveBrush(x1, y1, cp1x, cp1y, cp2x, cp2y, x2, y2, maxThick, penAngle, pressureType) {
    const steps = 250;
    const penWidth = maxThick;
    const penHeight = maxThick * 0.15;
    
    for (let i = 0; i <= steps; i++) {
        let t = i / steps;
        let inv = 1 - t;
        
        let px = inv*inv*inv*x1 + 3*inv*inv*t*cp1x + 3*inv*t*t*cp2x + t*t*t*x2;
        let py = inv*inv*inv*y1 + 3*inv*inv*t*cp1y + 3*inv*t*t*cp2y + t*t*t*y2;
        
        let pressure = 1.0;
        if (pressureType === 0) pressure = Math.sin(t * Math.PI);
        else if (pressureType === 1) pressure = Math.pow(1 - t, 0.5);
        else if (pressureType === 2) pressure = Math.pow(t, 0.5);
        else pressure = 0.8 + 0.2*Math.sin(t*Math.PI*3);
        
        let w = penWidth * Math.max(0.1, pressure);
        let h = penHeight * Math.max(0.1, pressure);
        
        ctx.beginPath();
        if (ctx.ellipse) {
            ctx.ellipse(px, py, w/2, h/2, penAngle, 0, Math.PI*2);
        } else {
            ctx.arc(px, py, w/2, 0, Math.PI*2); // fallback
        }
        ctx.fill();
    }
}

function paintWord(word, x, y, w, h, key) {
    const wordHash = hash(word.toLowerCase() + key);
    const color = PALETTE[wordHash % PALETTE.length];
    ctx.fillStyle = color;
    const r = rng(wordHash);
    
    const penAngle = -Math.PI/8 + r() * Math.PI/4;
    const numComponents = 3 + (r() * 3 | 0);
    
    let cx = x + w/2;
    let cy = y - h/2;
    
    for (let i=0; i<numComponents; i++) {
        let startX = cx + (r()-0.5)*w*0.8; let startY = cy + (r()-0.5)*h*0.8;
        let cp1x = cx + (r()-0.5)*w*1.0; let cp1y = cy + (r()-0.5)*h*1.0;
        let cp2x = cx + (r()-0.5)*w*1.0; let cp2y = cy + (r()-0.5)*h*1.0;
        let endX = cx + (r()-0.5)*w*0.8; let endY = cy + (r()-0.5)*h*0.8;
        
        let maxThick = w*0.09 + r()*w*0.04;
        let pressureType = r() * 4 | 0;
        drawCursiveBrush(startX, startY, cp1x, cp1y, cp2x, cp2y, endX, endY, maxThick, penAngle, pressureType);
    }
    
    const numDeco = 1 + (r() * 2 | 0);
    for (let i=0; i<numDeco; i++) {
        let type = r();
        let dx = x + w/2 + (r()-0.5)*w*0.8;
        let dy = y - h/2 + (r()-0.5)*h*0.8;
        let decoThick = w*0.08 + r()*w*0.05;
        
        if (type < 0.3) {
            ctx.beginPath();
            if (ctx.ellipse) ctx.ellipse(dx, dy, decoThick/2, decoThick/2, 0, 0, Math.PI*2);
            else ctx.arc(dx, dy, decoThick/2, 0, Math.PI*2);
            ctx.fill();
        } else if (type < 0.6) {
            let length = w*0.4 + r()*w*0.3;
            let angle = r() > 0.5 ? 0 : Math.PI/4;
            drawCursiveBrush(dx - Math.cos(angle)*length/2, dy - Math.sin(angle)*length/2, 
                             dx, dy, 
                             dx, dy, 
                             dx + Math.cos(angle)*length/2, dy + Math.sin(angle)*length/2, 
                             decoThick, penAngle, 1);
        } else {
            drawCursiveBrush(dx, dy, dx+w*0.3, dy-h*0.3, dx-w*0.3, dy+h*0.3, dx+w*0.1, dy+h*0.1, decoThick, penAngle, 0);
        }
    }
    return color;
}

function papyrusBorder(w, h, s, key) {
    ctx.fillStyle = 'rgb(30, 25, 22)'; ctx.fillRect(0, 0, w, h);
    const m = Math.max(22, w/18);
    const pageX = m, pageY = m/2, pageW = w - 2*m, pageH = h - m;
    
    ctx.fillStyle = 'rgb(234, 222, 203)';
    ctx.beginPath(); ctx.roundRect ? ctx.roundRect(pageX, pageY, pageW, pageH, 12) : ctx.rect(pageX, pageY, pageW, pageH); ctx.fill();
    
    const r = rng(hash(key + 'grain'));
    for (let i = 0; i < 4000; i++) {
        const a = 3 + (r() * 6 | 0);
        ctx.fillStyle = `rgba(160, 140, 120, ${a/255})`;
        ctx.fillRect(pageX + r()*pageW, pageY + r()*pageH, 1 + r()*2, 1 + r()*2);
    }
    
    const bw = 50 * s;
    ctx.fillStyle = 'rgb(215, 205, 185)';
    ctx.beginPath(); ctx.roundRect ? ctx.roundRect(pageX + 8, pageY + 8, pageW - 16, pageH - 16, 8) : ctx.rect(pageX+8, pageY+8, pageW-16, pageH-16); ctx.fill();
    ctx.fillStyle = 'rgb(234, 222, 203)';
    ctx.beginPath(); ctx.roundRect ? ctx.roundRect(pageX + bw, pageY + bw, pageW - 2*bw, pageH - 2*bw, 12) : ctx.rect(pageX+bw, pageY+bw, pageW-2*bw, pageH-2*bw); ctx.fill();
    
    const drawLotus = (x, y, scale, angle) => {
        ctx.save();
        ctx.translate(x, y);
        ctx.rotate(angle);
        
        // Stem
        ctx.strokeStyle = '#3d354f'; ctx.lineWidth = 1.5*scale;
        ctx.beginPath(); ctx.moveTo(0, 10*scale); ctx.lineTo(0, 25*scale); ctx.stroke();
        
        // Red side petals
        ctx.fillStyle = 'rgb(175, 55, 55)';
        ctx.beginPath(); ctx.moveTo(-6*scale, 0); ctx.lineTo(-18*scale, -22*scale); ctx.lineTo(-10*scale, 0); ctx.fill();
        ctx.beginPath(); ctx.moveTo(6*scale, 0); ctx.lineTo(18*scale, -22*scale); ctx.lineTo(10*scale, 0); ctx.fill();
        
        // Red center petal
        ctx.beginPath(); ctx.moveTo(-4*scale, 2*scale); ctx.lineTo(0, -25*scale); ctx.lineTo(4*scale, 2*scale); ctx.fill();

        // Blue cup
        ctx.fillStyle = 'rgb(100, 140, 150)';
        ctx.beginPath(); 
        ctx.moveTo(-16*scale, -8*scale);
        ctx.bezierCurveTo(-16*scale, 18*scale, 16*scale, 18*scale, 16*scale, -8*scale);
        ctx.closePath();
        ctx.fill();
        
        // Decorative spacing dot
        ctx.fillStyle = 'rgb(60, 50, 40)';
        ctx.beginPath(); ctx.arc(33*scale, 0, 3.5*scale, 0, Math.PI*2); ctx.fill();
        
        ctx.restore();
    };
    
    const marginCenterTop = pageY + bw/2;
    const marginCenterBottom = pageY + pageH - bw/2;
    const marginCenterLeft = pageX + bw/2;
    const marginCenterRight = pageX + pageW - bw/2;
    
    for (let bx = pageX + bw + 33*s; bx < pageX + pageW - bw - 30*s; bx += 66*s) {
        drawLotus(bx, marginCenterTop, s, Math.PI); // Points downward (into page)
        drawLotus(bx, marginCenterBottom, s, 0); // Points upward
    }
    for (let by = pageY + bw + 33*s; by < pageY + pageH - bw - 30*s; by += 66*s) {
        drawLotus(marginCenterLeft, by, s, Math.PI/2); // Points right
        drawLotus(marginCenterRight, by, s, -Math.PI/2); // Points left
    }
    
    const innerM = bw + 8*s;
    ctx.strokeStyle = 'rgb(150, 120, 90)'; ctx.lineWidth = 2.5*s;
    ctx.beginPath(); ctx.roundRect ? ctx.roundRect(pageX + innerM, pageY + innerM, pageW - 2*innerM, pageH - 2*innerM, 20) : ctx.rect(pageX+innerM, pageY+innerM, pageW-2*innerM, pageH-2*innerM); ctx.stroke();
    
    return { pageX, pageY, pageW, pageH, innerM };
}

function draw() {
    const key = seed.value || 'unnamed';
    const w = canvas.width, h = canvas.height, s = w/900;
    const { pageX, pageY, pageW, pageH, innerM } = papyrusBorder(w, h, s, key);
    
    const left = pageX + innerM + 40*s;
    const right = pageX + pageW - innerM - 40*s;
    const top = pageY + innerM + 260*s; // Pushed down to clear massive header
    
    const scriptKey = hash(key).toString(36).toUpperCase();
    const headerText = `THE  ${scriptKey}  FRAGMENT`;
    ctx.fillStyle = 'rgb(155, 40, 40)'; ctx.font = `bold ${22*s}px serif`;
    const headerW = ctx.measureText(headerText).width;
    ctx.fillText(headerText, pageX + (pageW - headerW)/2, pageY + innerM + 50*s);
    
    const footerText = "Codex rules: Colors distinguish root concepts. Calligraphic weight indicates stress.";
    ctx.fillStyle = 'rgb(120, 100, 80)'; ctx.font = `italic ${14*s}px serif`;
    const footerW = ctx.measureText(footerText).width;
    ctx.fillText(footerText, pageX + (pageW - footerW)/2, pageY + pageH - innerM - 25*s);
    
    translation.innerHTML = '';
    
    const lines = poem.value.split(/\r?\n/);
    let lineNo = 0;
    
    for (const line of lines) {
        const regex = /([a-zA-Z']+)|([^a-zA-Z']+)/g;
        let m, words = [];
        while ((m = regex.exec(line)) !== null) {
            words.push({ word: m[1], nonWord: m[2] });
        }
        
        let lineWidth = 0;
        for (const token of words) {
            if (token.word) lineWidth += (50*s + Math.min(4, token.word.length)*12*s) + 12*s;
            if (token.nonWord) {
                for(let c of token.nonWord) {
                    if (c===' '||c==='\t') lineWidth+=20*s; else lineWidth+=15*s;
                }
            }
        }
        if(words.length && words[words.length-1].word) lineWidth -= 12*s; // correct trailing space
        
        let startX = left + Math.max(0, (right - left - lineWidth)/2);
        let firstWordInLine = true;
        let yPos = top + lineNo * 110*s;
        
        for (const token of words) {
            if (token.word) {
                const gw = 50*s + Math.min(4, token.word.length)*12*s; // Slightly narrower width bounding
                const gh = 85*s;
                
                if (startX + gw <= right) {
                    const color = paintWord(token.word, startX, yPos, gw, gh, key);
                    
                    const span = document.createElement('span');
                    span.textContent = token.word;
                    span.style.color = color;
                    span.style.fontWeight = 'bold';
                    translation.appendChild(span);
                }
                startX += gw + 12*s;
                firstWordInLine = false;
            }
            if (token.nonWord) {
                const span = document.createElement('span');
                span.textContent = token.nonWord;
                span.style.color = 'rgb(130, 140, 140)';
                translation.appendChild(span);
                
                for (let c of token.nonWord) {
                    if (c===' '||c==='\t') startX += 20*s;
                    else {
                        if (startX <= right) {
                            ctx.fillStyle = 'rgba(180, 50, 50, 0.7)';
                            ctx.beginPath(); ctx.arc(startX, yPos - 10*s, 3.5*s, 0, Math.PI*2); ctx.fill();
                        }
                        startX += 15*s;
                    }
                }
            }
        }
        translation.appendChild(document.createElement('br'));
        lineNo++;
    }
    grammar.textContent = footerText;
}

document.querySelector('#transmute').addEventListener('click',draw); 
document.querySelector('#new-seed').addEventListener('click',()=>{seed.value=Math.random().toString(36).slice(2,10);draw()});
document.querySelector('#download').addEventListener('click',()=>{const a=document.createElement('a');a.download='asemic-codex.png';a.href=canvas.toDataURL('image/png');a.click();});

if (document.fonts) { document.fonts.ready.then(draw); } else { setTimeout(draw, 200); }

const titleText = document.querySelector('#title-text');
if (titleText) {
    titleText.addEventListener('mousemove', (e) => {
        const rect = titleText.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        titleText.style.setProperty('--x', `${x}px`);
        titleText.style.setProperty('--y', `${y}px`);
    });
}
