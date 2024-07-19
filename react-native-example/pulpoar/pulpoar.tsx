import React, {useImperativeHandle, useMemo, useRef, useState} from 'react';
import {ActivityIndicator, StyleSheet, View} from 'react-native';
import {WebView} from 'react-native-webview';
import {sdk} from './sdk.ts';
import {getInitialSdkScript, isEventType} from './utils.ts';
import type {PulpoARProps, EventKeys, ActionKeys, Events} from './types.ts';

export function PulpoAR({plugin, slug, actionsRef, ...rest}: PulpoARProps) {
  const pluginRef = useRef<WebView>(null);
  const [iframeLoaded, setIframeLoaded] = useState(false);

  useImperativeHandle(
    actionsRef,
    () => {
      const actions: Record<ActionKeys, (param: any) => string> = {} as any;
      for (const actionName of Object.keys(sdk.actions) as ActionKeys[]) {
        actions[actionName] = (param: any) => {
          const actionFunction = sdk.actions[actionName](param);
          pluginRef.current?.injectJavaScript(actionFunction);
          return actionFunction;
        };
      }
      return actions;
    },
    [],
  );

  const events: Partial<Events> = useMemo(
    () =>
      Object.entries(rest).reduce((acc, [key, value]) => {
        if (isEventType(key)) {
          return {...acc, [key]: value};
        }
        return acc;
      }, {}),
    [rest],
  );

  return (
    <View style={styles.root}>
      {!iframeLoaded && (
        <View style={styles.indicatorStyle}>
          <ActivityIndicator />
        </View>
      )}
      <WebView
        ref={pluginRef}
        originWhitelist={['*']}
        allowsAirPlayForMediaPlayback={true}
        allowsProtectedMedia={true}
        mediaPlaybackRequiresUserAction={false}
        source={{
          uri: `https://plugin.pulpoar.com/${plugin}/${slug}`,
        }}
        injectedJavaScript={getInitialSdkScript(events)}
        onMessage={payload => {
          const parsed = JSON.parse(payload.nativeEvent.data) as {
            event: EventKeys;
            payload: never;
          };
          const event = parsed.event;
          events[event]?.(parsed.payload);
        }}
        containerStyle={styles.containerStyle}
        onLoadEnd={() => setIframeLoaded(true)}
        allowsInlineMediaPlayback
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    position: 'relative',
  },
  indicatorStyle: {flex: 1, justifyContent: 'center', alignItems: 'center'},
  containerStyle: {
    zIndex: -5,
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    position: 'absolute',
  },
});
