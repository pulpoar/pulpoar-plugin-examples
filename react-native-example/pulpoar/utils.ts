import type {Events, EventKeys} from './types.ts';
import {sdk} from './sdk';

export function getInitialSdkScript(events: Partial<Events>) {
  return `
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/@pulpoar/plugin-sdk@latest/dist/index.iife.js';
    script.onload = function() {
      ${
        events &&
        Object.keys(events)
          .map(event => makeSdkEvent(event as EventKeys))
          .join('\n')
      }
    }
    document.body.appendChild(script);
  `;
}

export function makeSdkEvent(eventName: EventKeys) {
  return `
    pulpoar['${eventName}']((payload)=>{
       window.ReactNativeWebView.postMessage(JSON.stringify({event: "${eventName}" ,payload}));
    });`;
}

export const isEventType = (key: string): key is EventKeys => {
  return Object.keys(sdk.events).includes(key);
};
