parcelRequire=function(e,r,n,t){var i="function"==typeof parcelRequire&&parcelRequire,o="function"==typeof require&&require;function u(n,t){if(!r[n]){if(!e[n]){var f="function"==typeof parcelRequire&&parcelRequire;if(!t&&f)return f(n,!0);if(i)return i(n,!0);if(o&&"string"==typeof n)return o(n);var c=new Error("Cannot find module '"+n+"'");throw c.code="MODULE_NOT_FOUND",c}p.resolve=function(r){return e[n][1][r]||r};var l=r[n]=new u.Module(n);e[n][0].call(l.exports,p,l,l.exports,this)}return r[n].exports;function p(e){return u(p.resolve(e))}}u.isParcelRequire=!0,u.Module=function(e){this.id=e,this.bundle=u,this.exports={}},u.modules=e,u.cache=r,u.parent=i,u.register=function(r,n){e[r]=[function(e,r){r.exports=n},{}]};for(var f=0;f<n.length;f++)u(n[f]);if(n.length){var c=u(n[n.length-1]);"object"==typeof exports&&"undefined"!=typeof module?module.exports=c:"function"==typeof define&&define.amd?define(function(){return c}):t&&(this[t]=c)}return u}({"F1Ig":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.initialState={error:null,me:null,rooms:[],current:null,lastEvent:null};
},{}],"RttD":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});const e=new WeakMap,t=exports.directive=(t=>(e.set(t,!0),t)),s=exports.isDirective=(t=>"function"==typeof t&&e.has(t));
},{}],"IOgS":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});const e=exports.isCEPolyfill=void 0!==window.customElements&&void 0!==window.customElements.polyfillWrapFlushCallback,l=exports.reparentNodes=((e,l,o=null,t=null)=>{let s=l;for(;s!==o;){const l=s.nextSibling;e.insertBefore(s,t),s=l}}),o=exports.removeNodes=((e,l,o=null)=>{let t=l;for(;t!==o;){const l=t.nextSibling;e.removeChild(t),t=l}});
},{}],"SWq1":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});const e=exports.noChange={};
},{}],"WS92":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});const e=exports.marker=`{{lit-${String(Math.random()).slice(2)}}}`,t=exports.nodeMarker=`\x3c!--${e}--\x3e`,r=exports.markerRegex=new RegExp(`${e}|${t}`),s=exports.rewritesStyleAttribute=(()=>{const e=document.createElement("div");return e.setAttribute("style","{{bad value}}"),"{{bad value}}"!==e.getAttribute("style")})();class n{constructor(t,n){this.parts=[],this.element=n;let o=-1,a=0;const l=[],d=n=>{const c=n.content,p=document.createTreeWalker(c,133,null,!1);let u,f;for(;p.nextNode();){o++,u=f;const n=f=p.currentNode;if(1===n.nodeType){if(n.hasAttributes()){const i=n.attributes;let l=0;for(let t=0;t<i.length;t++)i[t].value.indexOf(e)>=0&&l++;for(;l-- >0;){const e=t.strings[a],i=x.exec(e)[2],l=s&&"style"===i?"style$":/^[a-zA-Z-]*$/.test(i)?i:i.toLowerCase(),d=n.getAttribute(l).split(r);this.parts.push({type:"attribute",index:o,name:i,strings:d}),n.removeAttribute(l),a+=d.length-1}}"TEMPLATE"===n.tagName&&d(n)}else if(3===n.nodeType){const t=n.nodeValue;if(t.indexOf(e)<0)continue;const s=n.parentNode,x=t.split(r),d=x.length-1;a+=d;for(let e=0;e<d;e++)s.insertBefore(""===x[e]?i():document.createTextNode(x[e]),n),this.parts.push({type:"node",index:o++});s.insertBefore(""===x[d]?i():document.createTextNode(x[d]),n),l.push(n)}else if(8===n.nodeType)if(n.nodeValue===e){const e=n.parentNode,t=n.previousSibling;null===t||t!==u||t.nodeType!==Node.TEXT_NODE?e.insertBefore(i(),n):o--,this.parts.push({type:"node",index:o++}),l.push(n),null===n.nextSibling?e.insertBefore(i(),n):o--,f=u,a++}else{let t=-1;for(;-1!==(t=n.nodeValue.indexOf(e,t+1));)this.parts.push({type:"node",index:-1})}}};d(n);for(const e of l)e.parentNode.removeChild(e)}}exports.Template=n;const o=exports.isTemplatePartActive=(e=>-1!==e.index),i=exports.createMarker=(()=>document.createComment("")),x=exports.lastAttributeNameRegex=/([ \x09\x0a\x0c\x0d])([^\0-\x1F\x7F-\x9F \x09\x0a\x0c\x0d"'>=\/]+)([ \x09\x0a\x0c\x0d]*=[ \x09\x0a\x0c\x0d]*(?:[^ \x09\x0a\x0c\x0d"'`<>=]*|"[^"]*|'[^']*))$/;
},{}],"nlQu":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.TemplateInstance=void 0;var e=require("./dom.js"),t=require("./template.js");class s{constructor(e,t,s){this._parts=[],this.template=e,this.processor=t,this._getTemplate=s}update(e){let t=0;for(const s of this._parts)void 0!==s&&s.setValue(e[t]),t++;for(const e of this._parts)void 0!==e&&e.commit()}_clone(){const s=e.isCEPolyfill?this.template.element.content.cloneNode(!0):document.importNode(this.template.element.content,!0),o=this.template.parts;let n=0,r=0;const i=e=>{const s=document.createTreeWalker(e,133,null,!1);let l=s.nextNode();for(;n<o.length&&null!==l;){const e=o[n];if((0,t.isTemplatePartActive)(e))if(r===e.index){if("node"===e.type){const e=this.processor.handleTextExpression(this._getTemplate);e.insertAfterNode(l),this._parts.push(e)}else this._parts.push(...this.processor.handleAttributeExpressions(l,e.name,e.strings));n++}else r++,"TEMPLATE"===l.nodeName&&i(l.content),l=s.nextNode();else this._parts.push(void 0),n++}};return i(s),e.isCEPolyfill&&(document.adoptNode(s),customElements.upgrade(s)),s}}exports.TemplateInstance=s;
},{"./dom.js":"IOgS","./template.js":"WS92"}],"J5hk":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.SVGTemplateResult=exports.TemplateResult=void 0;var e=require("./dom.js"),t=require("./template.js");class s{constructor(e,t,s,r){this.strings=e,this.values=t,this.type=s,this.processor=r}getHTML(){const e=this.strings.length-1;let s="",r=!0;for(let l=0;l<e;l++){const e=this.strings[l];s+=e;const n=e.lastIndexOf(">");!(r=(n>-1||r)&&-1===e.indexOf("<",n+1))&&t.rewritesStyleAttribute&&(s=s.replace(t.lastAttributeNameRegex,(e,t,s,r)=>"style"===s?`${t}style$${r}`:e)),s+=r?t.nodeMarker:t.marker}return s+=this.strings[e]}getTemplateElement(){const e=document.createElement("template");return e.innerHTML=this.getHTML(),e}}exports.TemplateResult=s;class r extends s{getHTML(){return`<svg>${super.getHTML()}</svg>`}getTemplateElement(){const t=super.getTemplateElement(),s=t.content,r=s.firstChild;return s.removeChild(r),(0,e.reparentNodes)(s,r.firstChild),t}}exports.SVGTemplateResult=r;
},{"./dom.js":"IOgS","./template.js":"WS92"}],"Xs9h":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.EventPart=exports.PropertyPart=exports.PropertyCommitter=exports.BooleanAttributePart=exports.NodePart=exports.AttributePart=exports.AttributeCommitter=exports.isPrimitive=void 0;var t=require("./directive.js"),e=require("./dom.js"),i=require("./part.js"),s=require("./template-instance.js"),n=require("./template-result.js"),r=require("./template.js");const a=exports.isPrimitive=(t=>null===t||!("object"==typeof t||"function"==typeof t));class o{constructor(t,e,i){this.dirty=!0,this.element=t,this.name=e,this.strings=i,this.parts=[];for(let t=0;t<i.length-1;t++)this.parts[t]=this._createPart()}_createPart(){return new h(this)}_getValue(){const t=this.strings,e=t.length-1;let i="";for(let s=0;s<e;s++){i+=t[s];const e=this.parts[s];if(void 0!==e){const t=e.value;if(null!=t&&(Array.isArray(t)||"string"!=typeof t&&t[Symbol.iterator]))for(const e of t)i+="string"==typeof e?e:String(e);else i+="string"==typeof t?t:String(t)}}return i+=t[e]}commit(){this.dirty&&(this.dirty=!1,this.element.setAttribute(this.name,this._getValue()))}}exports.AttributeCommitter=o;class h{constructor(t){this.value=void 0,this.committer=t}setValue(e){e===i.noChange||a(e)&&e===this.value||(this.value=e,(0,t.isDirective)(e)||(this.committer.dirty=!0))}commit(){for(;(0,t.isDirective)(this.value);){const t=this.value;this.value=i.noChange,t(this)}this.value!==i.noChange&&this.committer.commit()}}exports.AttributePart=h;class l{constructor(t){this.value=void 0,this._pendingValue=void 0,this.templateFactory=t}appendInto(t){this.startNode=t.appendChild((0,r.createMarker)()),this.endNode=t.appendChild((0,r.createMarker)())}insertAfterNode(t){this.startNode=t,this.endNode=t.nextSibling}appendIntoPart(t){t._insert(this.startNode=(0,r.createMarker)()),t._insert(this.endNode=(0,r.createMarker)())}insertAfterPart(t){t._insert(this.startNode=(0,r.createMarker)()),this.endNode=t.endNode,t.endNode=this.startNode}setValue(t){this._pendingValue=t}commit(){for(;(0,t.isDirective)(this._pendingValue);){const t=this._pendingValue;this._pendingValue=i.noChange,t(this)}const e=this._pendingValue;e!==i.noChange&&(a(e)?e!==this.value&&this._commitText(e):e instanceof n.TemplateResult?this._commitTemplateResult(e):e instanceof Node?this._commitNode(e):Array.isArray(e)||e[Symbol.iterator]?this._commitIterable(e):void 0!==e.then?this._commitPromise(e):this._commitText(e))}_insert(t){this.endNode.parentNode.insertBefore(t,this.endNode)}_commitNode(t){this.value!==t&&(this.clear(),this._insert(t),this.value=t)}_commitText(t){const e=this.startNode.nextSibling;t=null==t?"":t,e===this.endNode.previousSibling&&e.nodeType===Node.TEXT_NODE?e.textContent=t:this._commitNode(document.createTextNode("string"==typeof t?t:String(t))),this.value=t}_commitTemplateResult(t){const e=this.templateFactory(t);if(this.value&&this.value.template===e)this.value.update(t.values);else{const i=new s.TemplateInstance(e,t.processor,this.templateFactory),n=i._clone();i.update(t.values),this._commitNode(n),this.value=i}}_commitIterable(t){Array.isArray(this.value)||(this.value=[],this.clear());const e=this.value;let i,s=0;for(const n of t)void 0===(i=e[s])&&(i=new l(this.templateFactory),e.push(i),0===s?i.appendIntoPart(this):i.insertAfterPart(e[s-1])),i.setValue(n),i.commit(),s++;s<e.length&&(e.length=s,this.clear(i&&i.endNode))}_commitPromise(t){this.value=t,t.then(e=>{this.value===t&&(this.setValue(e),this.commit())})}clear(t=this.startNode){(0,e.removeNodes)(this.startNode.parentNode,t.nextSibling,this.endNode)}}exports.NodePart=l;class u{constructor(t,e,i){if(this.value=void 0,this._pendingValue=void 0,2!==i.length||""!==i[0]||""!==i[1])throw new Error("Boolean attributes can only contain a single expression");this.element=t,this.name=e,this.strings=i}setValue(t){this._pendingValue=t}commit(){for(;(0,t.isDirective)(this._pendingValue);){const t=this._pendingValue;this._pendingValue=i.noChange,t(this)}if(this._pendingValue===i.noChange)return;const e=!!this._pendingValue;this.value!==e&&(e?this.element.setAttribute(this.name,""):this.element.removeAttribute(this.name)),this.value=e,this._pendingValue=i.noChange}}exports.BooleanAttributePart=u;class d extends o{constructor(t,e,i){super(t,e,i),this.single=2===i.length&&""===i[0]&&""===i[1]}_createPart(){return new m(this)}_getValue(){return this.single?this.parts[0].value:super._getValue()}commit(){this.dirty&&(this.dirty=!1,this.element[this.name]=this._getValue())}}exports.PropertyCommitter=d;class m extends h{}exports.PropertyPart=m;class c{constructor(t,e){this.value=void 0,this._pendingValue=void 0,this.element=t,this.eventName=e}setValue(t){this._pendingValue=t}commit(){for(;(0,t.isDirective)(this._pendingValue);){const t=this._pendingValue;this._pendingValue=i.noChange,t(this)}this._pendingValue!==i.noChange&&(null==this._pendingValue!=(null==this.value)&&(null==this._pendingValue?this.element.removeEventListener(this.eventName,this):this.element.addEventListener(this.eventName,this)),this.value=this._pendingValue,this._pendingValue=i.noChange)}handleEvent(t){"function"==typeof this.value?this.value.call(this.element,t):"function"==typeof this.value.handleEvent&&this.value.handleEvent(t)}}exports.EventPart=c;
},{"./directive.js":"RttD","./dom.js":"IOgS","./part.js":"SWq1","./template-instance.js":"nlQu","./template-result.js":"J5hk","./template.js":"WS92"}],"ydwG":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.defaultTemplateProcessor=exports.DefaultTemplateProcessor=void 0;var e=require("./parts.js");class t{handleAttributeExpressions(t,r,s){const o=r[0];if("."===o){return new e.PropertyCommitter(t,r.slice(1),s).parts}return"@"===o?[new e.EventPart(t,r.slice(1))]:"?"===o?[new e.BooleanAttributePart(t,r.slice(1),s)]:new e.AttributeCommitter(t,r,s).parts}handleTextExpression(t){return new e.NodePart(t)}}exports.DefaultTemplateProcessor=t;const r=exports.defaultTemplateProcessor=new t;
},{"./parts.js":"Xs9h"}],"/Gih":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.templateCaches=void 0,exports.templateFactory=t;var e=require("./template.js");function t(t){let p=s.get(t.type);void 0===p&&(p=new Map,s.set(t.type,p));let r=p.get(t.strings);return void 0===r&&(r=new e.Template(t,t.getTemplateElement()),p.set(t.strings,r)),r}const s=exports.templateCaches=new Map;
},{"./template.js":"WS92"}],"OA5Z":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.parts=void 0,exports.render=s;var e=require("./dom.js"),t=require("./parts.js"),r=require("./template-factory.js");const o=exports.parts=new WeakMap;function s(s,a,p=r.templateFactory){let i=o.get(a);void 0===i&&((0,e.removeNodes)(a,a.firstChild),o.set(a,i=new t.NodePart(p)),i.appendInto(a)),i.setValue(s),i.commit()}
},{"./dom.js":"IOgS","./parts.js":"Xs9h","./template-factory.js":"/Gih"}],"QcWR":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.svg=exports.html=void 0;var e=require("./lib/template-result.js");Object.keys(e).forEach(function(t){"default"!==t&&"__esModule"!==t&&Object.defineProperty(exports,t,{enumerable:!0,get:function(){return e[t]}})});var t=require("./lib/template.js");Object.keys(t).forEach(function(e){"default"!==e&&"__esModule"!==e&&Object.defineProperty(exports,e,{enumerable:!0,get:function(){return t[e]}})});var r=require("./lib/default-template-processor.js");Object.keys(r).forEach(function(e){"default"!==e&&"__esModule"!==e&&Object.defineProperty(exports,e,{enumerable:!0,get:function(){return r[e]}})});var u=require("./lib/template-instance.js");Object.keys(u).forEach(function(e){"default"!==e&&"__esModule"!==e&&Object.defineProperty(exports,e,{enumerable:!0,get:function(){return u[e]}})});var n=require("./lib/part.js");Object.keys(n).forEach(function(e){"default"!==e&&"__esModule"!==e&&Object.defineProperty(exports,e,{enumerable:!0,get:function(){return n[e]}})});var o=require("./lib/parts.js");Object.keys(o).forEach(function(e){"default"!==e&&"__esModule"!==e&&Object.defineProperty(exports,e,{enumerable:!0,get:function(){return o[e]}})});var s=require("./lib/dom.js");Object.keys(s).forEach(function(e){"default"!==e&&"__esModule"!==e&&Object.defineProperty(exports,e,{enumerable:!0,get:function(){return s[e]}})});var l=require("./lib/directive.js");Object.keys(l).forEach(function(e){"default"!==e&&"__esModule"!==e&&Object.defineProperty(exports,e,{enumerable:!0,get:function(){return l[e]}})});var c=require("./lib/render.js");Object.keys(c).forEach(function(e){"default"!==e&&"__esModule"!==e&&Object.defineProperty(exports,e,{enumerable:!0,get:function(){return c[e]}})});var a=require("./lib/template-factory.js");Object.keys(a).forEach(function(e){"default"!==e&&"__esModule"!==e&&Object.defineProperty(exports,e,{enumerable:!0,get:function(){return a[e]}})});const i=exports.html=((t,...u)=>new e.TemplateResult(t,u,"html",r.defaultTemplateProcessor)),f=exports.svg=((t,...u)=>new e.SVGTemplateResult(t,u,"svg",r.defaultTemplateProcessor));
},{"./lib/default-template-processor.js":"ydwG","./lib/template-result.js":"J5hk","./lib/template.js":"WS92","./lib/template-instance.js":"nlQu","./lib/part.js":"SWq1","./lib/parts.js":"Xs9h","./lib/dom.js":"IOgS","./lib/directive.js":"RttD","./lib/render.js":"OA5Z","./lib/template-factory.js":"/Gih"}],"5WeN":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.when=void 0;var e=require("../lit-html.js");const t=new WeakMap,a=exports.when=((a,r,n)=>(0,e.directive)(o=>{let d=t.get(o);void 0===d&&(d={truePart:new e.NodePart(o.templateFactory),falsePart:new e.NodePart(o.templateFactory),cacheContainer:document.createDocumentFragment()},t.set(o,d),d.truePart.appendIntoPart(o),d.falsePart.appendIntoPart(o));const i=a?d.truePart:d.falsePart,s=a?r():n();if(!!a!==d.prevCondition){const t=a?d.falsePart:d.truePart;i.value&&o.startNode.parentNode.appendChild(d.cacheContainer),t.value&&(0,e.reparentNodes)(d.cacheContainer,t.startNode,t.endNode.nextSibling)}i.setValue(s),i.commit(),d.prevCondition=!!a}));
},{"../lit-html.js":"QcWR"}],"W65T":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});class e{constructor(e,t){this.url=e,this.listener=t}get state(){return this._state}set state(e){this._state=e}handle(e){return e.ok?e.json():e.text().then(e=>Promise.reject(e))}getJson(e){return fetch(this.url+e).then(e=>this.handle(e))}postJson(e,t){const o={method:"POST",body:JSON.stringify(t)};return fetch(this.url+e,o).then(e=>this.handle(e))}registerWS(e,t){const o=this.url.replace("http://","ws://"),s=new WebSocket(o+`/ws/${e}`);s.onopen=(e=>{console.log("WS open",e),s.send(JSON.stringify({playerId:t}))}),s.onmessage=(e=>{console.log("WS message",e);const t=JSON.parse(e.data);this.listener(t,this.state)}),s.onclose=(e=>{console.log("WS close",e)}),s.onerror=(e=>{console.error("WS error",e)})}login(e){return this.postJson("/api/auth/login",{name:e})}logout(e){return this.postJson("/api/auth/logout",{playerId:e})}getRooms(){return this.getJson("/api/room")}join(e,t){return this.postJson("/api/room/join",{roomId:e,playerId:t}).then(o=>(this.registerWS(e,t),o))}leave(e,t){return this.postJson("/api/room/leave",{roomId:e,playerId:t})}action(e,t,o){return this.postJson("/api/room/move",{roomId:e,playerId:t,action:o})}}exports.BackendApi=e;
},{}],"hfHN":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});const e=require("lit-html"),t=require("lit-html/directives/when"),a=require("../models/state"),n=require("./app"),r=e=>t=>{t.preventDefault();const a=t.target.name.value;return Promise.all([n.api.login(a),n.api.getRooms()]).then(([t,a])=>n.updateState(Object.assign({},e,{me:t,rooms:a,error:null}))).catch(t=>n.updateState(Object.assign({},e,{error:t}))),!1},o=e=>()=>n.api.logout(e.me.id).then(()=>n.updateState(a.initialState)).catch(t=>n.updateState(Object.assign({},e,{error:t}))),i=t=>()=>e.html`
    <div class="me">
        <span class="name">${t.me.name}</span>, 
        <span class="score">${t.me.score}</span>
        <button type="button" @click=${o(t)}>Logout</button>
    </div>`,s=t=>()=>e.html`
      <form name="login" @submit='${r(t)}'>
        <label> Login <input name='name' value='' required></label>
        <button>Login</button>
      </form>`;exports.loginTemplate=(a=>e.html`${t.when(a.me,i(a),s(a))}`);
},{"lit-html":"QcWR","lit-html/directives/when":"5WeN","../models/state":"F1Ig","./app":"CzcK"}],"hiG+":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.repeat=o;var e=require("../lit-html.js");const t=new WeakMap;function r(e,t,r){e.startNode.parentNode||r.delete(t)}function o(o,n,s){let d;return 2===arguments.length?s=n:3===arguments.length&&(d=n),(0,e.directive)(n=>{let a=t.get(n);void 0===a&&(a=new Map,t.set(n,a));const i=n.startNode.parentNode;let l=-1,c=n.startNode.nextSibling;for(const t of o){let r,o;try{r=s(t,++l),o=d?d(t):l}catch(e){console.error(e);continue}let N=a.get(o);if(void 0===N){const t=(0,e.createMarker)(),r=(0,e.createMarker)();i.insertBefore(t,c),i.insertBefore(r,c),(N=new e.NodePart(n.templateFactory)).insertAfterNode(t),void 0!==o&&a.set(o,N)}else if(c!==N.startNode){const t=N.endNode.nextSibling;c!==t&&(0,e.reparentNodes)(i,N.startNode,t,c)}else c=N.endNode.nextSibling;N.setValue(r),N.commit()}c!==n.endNode&&((0,e.removeNodes)(i,c,n.endNode),a.forEach(r))})}
},{"../lit-html.js":"QcWR"}],"BuVg":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});const e=require("lit-html"),t=require("lit-html/directives/when"),l=require("lit-html/directives/repeat"),i=require("./app"),s=(e,t)=>()=>i.api.join(e.id,t.me.id).then(e=>i.updateState(Object.assign({},t,{current:e}))).catch(e=>i.updateState(Object.assign({},t,{error:e}))),a=(l,i)=>e.html`
<div class="room">
  <div class="name">
    <h2>${l.name}</h2>
    <div class="players">${l.players.map(e=>e.player.name).join(", ")}</div>
  </div>
  <div class="status ${l.full?"full":""}">${l.players.length} / 4</div>
  <div class="action">
    ${t.when(l.full,()=>e.html`Full`,()=>e.html`<button type="button" class="join" @click=${s(l,i)}>Join</button>`)}
  </div>
</div>`;exports.roomsTemplate=(t=>e.html`<ul class="rooms">
  ${l.repeat(t.rooms,e=>e.name,l=>e.html`<li>${a(l,t)}</li>`)}
</ul>`);
},{"lit-html":"QcWR","lit-html/directives/when":"5WeN","lit-html/directives/repeat":"hiG+","./app":"CzcK"}],"XpzZ":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0}),exports.actionLabel=(e=>{switch(e){case"draw":return"Draw";case"stay":return"Stay";default:return"???"}});
},{}],"mj0V":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});const e=require("lit-html"),t=require("lit-html/directives/when"),a=require("lit-html/directives/repeat"),s=require("../models/models"),i=require("./app"),r=e=>()=>i.api.leave(e.current.id,e.me.id).then(()=>i.api.getRooms()).then(t=>i.updateState(Object.assign({},e,{rooms:t,current:null}))).catch(t=>i.updateState(Object.assign({},e,{error:t}))),d=(e,t)=>()=>i.api.action(e.current.id,e.me.id,t).then(t=>i.updateState(Object.assign({},e,{current:t}))).catch(t=>i.updateState(Object.assign({},e,{error:t}))),l=t=>e.html`${a.repeat(t.cards,e=>e,t=>e.html`
<div class="card">
  <img src=${`https://deckofcardsapi.com/static/img/${t}.png`} alt=${t}>
</div>`)}`,c=({player:t,status:a})=>e.html`
<div class="player ${a.move}">
    <div class="name">${t.name}</div>
    <div class="score">${a.hand.score}</div>
    <div class="move">${a.move}</div>
    <div class="cards">${l(a.hand)}</div>
  </div>`,n=t=>()=>{let i=t.current;const n=t.me&&i.players.find(e=>e.player.id===t.me.id),v=t.lastEvent&&t.lastEvent.winners?t.lastEvent.winners.length?t.lastEvent.winners.map(e=>e.name).join(", "):"Bank":null;return e.html`
<div class="room-current">
  <header>
    <div class="name">${i.name}</div>
    <button type="button" @click=${r(t)}>Leave</button>
  </header>
  
  ${v?e.html`<h3 class="winner">${v}</h3>`:""}
  
  <div class="players">
      <!--bank-->
      <div class="player bank">
        <div class="name">Bank</div>
        <div class="score">${i.bank.score}</div>
        <div class="cards">${l(i.bank)}</div>
        <div class="move"></div>
      </div>
        
      <!--other players-->
      ${a.repeat(i.players.filter(e=>!t.me||e.player.id!==t.me.id),e=>e.player.id,e=>c(e))}
      
      <!-- me -->
      ${n?e.html`
      <div class="player me ${n.status.move}">
        <div class="name">${n.player.name}</div>
        <div class="score">${n.status.hand.score}</div>
        <div class="move">${n.status.move}</div>
        <div class="cards">${l(n.status.hand)}</div>
        <div class="actions">
          ${a.repeat(n.status.canDo,e=>e,a=>e.html`<button type="button" @click=${d(t,a)}>
               ${s.actionLabel(a)}
             </button>`)}
        </div>
      </div>`:""}
      </div>
</div>`};exports.currentTemplate=(a=>e.html`${t.when(a.current,n(a),()=>e.html``)}`);
},{"lit-html":"QcWR","lit-html/directives/when":"5WeN","lit-html/directives/repeat":"hiG+","../models/models":"XpzZ","./app":"CzcK"}],"CzcK":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});const e=require("lit-html"),t=require("lit-html/directives/when"),r=require("../services/BackendApi"),a=require("./login"),n=require("./rooms"),o=require("./current"),s=document.querySelector("body > header progress");let l;const c=()=>{clearInterval(l),s.value=0,l=setInterval(()=>s.value+=1,500)},u=()=>{clearInterval(l),s.value=0},i=(e,t)=>{switch(console.debug("event",e.type),e.type){case"turn-started":exports.updateState(Object.assign({},t,{current:e.room,lastEvent:e})),c();break;case"turn-ended":const r=e.room.players.find(e=>e.player.id===t.me.id)?e.room:null;exports.updateState(Object.assign({},t,{current:r,lastEvent:e})),c();break;case"player-joining":case"player-leaving":case"player-action":exports.updateState(Object.assign({},t,{current:e.room}));break;case"round-ended":clearInterval(l),exports.updateState(Object.assign({},t,{current:e.room,lastEvent:e})),clearInterval(l),s.value=0;break;default:console.warn(`type: ${e.type} not supported`,e)}};exports.api=new r.BackendApi("http://ilaborie.org:9898",i);const p=document.querySelector("main"),d=t=>()=>e.html`
<div class="error">
  ${t.error}
  <button class="button" @click=${()=>exports.updateState(Object.assign({},t,{error:null}))}>Close</button>
</div>`,m=e=>null===e.me?a.loginTemplate(e):e.current?o.currentTemplate(e):n.roomsTemplate(e),v=r=>e.html`
${t.when(r.error,d(r),()=>e.html`<section>${m(r)}</section>`)}`;exports.updateState=(t=>{console.debug("updateState",t),exports.api.state=t,e.render(v(t),p)});
},{"lit-html":"QcWR","lit-html/directives/when":"5WeN","../services/BackendApi":"W65T","./login":"hfHN","./rooms":"BuVg","./current":"mj0V"}],"7QCb":[function(require,module,exports) {
"use strict";Object.defineProperty(exports,"__esModule",{value:!0});const e=require("./models/state"),t=require("./templates/app");t.updateState(e.initialState);
},{"./models/state":"F1Ig","./templates/app":"CzcK"}]},{},["7QCb"], null)
//# sourceMappingURL=/web.4f1f9d31.map