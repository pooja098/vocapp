import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';

export const updateRoundTime = (
  nextTime: number,
  setRoundTime: React.Dispatch<React.SetStateAction<number>>,
  setTimeLeft: React.Dispatch<React.SetStateAction<number>>,
): void => {
  setRoundTime(nextTime);
  setTimeLeft(nextTime);
};

export const updateTotalPairs = (
  nextTotal: number,
  setTotalPairsToWin: React.Dispatch<React.SetStateAction<number>>,
): void => {
  setTotalPairsToWin(nextTotal);
};

interface SettingsScreenProps {
  roundTime: number;
  totalPairsToWin: number;
  onRoundTimeChange: (nextTime: number) => void;
  onTotalPairsChange: (nextTotal: number) => void;
  onStartNewRound: () => void;
}

export default function SettingsScreen({
  roundTime = 45,
  totalPairsToWin = 10,
  onRoundTimeChange,
  onTotalPairsChange,
  onStartNewRound,
}: SettingsScreenProps): React.JSX.Element {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Game Settings</Text>

      <Text style={styles.label}>Round Timer: {roundTime} seconds</Text>
      <View style={styles.row}>
        <TouchableOpacity style={styles.configBtn} onPress={() => onRoundTimeChange(Math.max(15, roundTime - 15))}>
          <Text style={styles.configBtnTxt}>-15s</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.configBtn} onPress={() => onRoundTimeChange(roundTime + 15)}>
          <Text style={styles.configBtnTxt}>+15s</Text>
        </TouchableOpacity>
      </View>

      <Text style={styles.label}>Total Pairs per Session: {totalPairsToWin}</Text>
      <View style={styles.row}>
        <TouchableOpacity style={styles.configBtn} onPress={() => onTotalPairsChange(Math.max(5, totalPairsToWin - 1))}>
          <Text style={styles.configBtnTxt}>-1 Pair</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.configBtn}
          onPress={() => onTotalPairsChange(Math.min(12, totalPairsToWin + 1))}
        >
          <Text style={styles.configBtnTxt}>+1 Pair</Text>
        </TouchableOpacity>
      </View>

      <TouchableOpacity style={styles.primaryButton} onPress={onStartNewRound}>
        <Text style={styles.buttonText}>Start New Round</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: '#131F24' },
  title: { fontSize: 28, fontWeight: 'bold', color: '#FFF', textAlign: 'center', marginVertical: 20 },
  label: { fontSize: 16, color: '#B0B0B0', marginTop: 25, marginBottom: 10, fontWeight: '600' },
  row: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 10 },
  configBtn: { backgroundColor: '#202F36', flex: 0.48, padding: 12, borderRadius: 8, alignItems: 'center' },
  configBtnTxt: { color: '#FFF', fontWeight: 'bold' },
  primaryButton: { backgroundColor: '#58CC02', padding: 15, borderRadius: 12, alignItems: 'center', marginTop: 20 },
  buttonText: { color: '#FFF', fontSize: 18, fontWeight: 'bold' },
});
