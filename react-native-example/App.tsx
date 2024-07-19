import React, {useRef} from 'react';
import {Button, SafeAreaView, StyleSheet} from 'react-native';
import {PulpoAR, Actions} from './pulpoar';

function App(): React.JSX.Element {
  const actionsRef = useRef<Actions>(null);

  return (
    <SafeAreaView style={styles.wrapper}>
      <PulpoAR
        plugin="vto"
        slug="makeup"
        actionsRef={actionsRef}
        onReady={payload => console.log('PulpoAR is ready', payload)}
        onAddToCart={payload => console.log('Add to Cart clicked', payload)}
        onPathChange={payload => console.log('Path changed', payload)}
        onAppliedVariantsChange={payload =>
          console.log('Applied variants changed', payload)
        }
      />
      <Button
        title="Set Path"
        onPress={() => {
          actionsRef.current?.setPath('choose-model');
        }}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({wrapper: {flex: 1}});

export default App;
