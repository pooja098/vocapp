import React from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';

export interface PastGame {
  id: number;
  date: string;
  result: 'Won' | 'Lost';
  score: string;
  time: string;
  elapsedSeconds?: number;
}

export interface ResultSummary {
  isWin: boolean;
  score: string;
  time: string;
  message: string;
}

interface ResultsScreenProps {
  lastResult: ResultSummary | null;
  pastGames: PastGame[];
}

export default function ResultsScreen({ lastResult, pastGames = [] }: ResultsScreenProps): React.JSX.Element {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Results</Text>

      <Text style={styles.sectionHeader}>Latest Match</Text>
      {lastResult ? (
        <View style={styles.resultCard}>
          <Text style={[styles.resultStatus, lastResult.isWin ? styles.won : styles.lost]}>
            {lastResult.isWin ? 'Won' : 'Lost'}
          </Text>
          <Text style={styles.resultScore}>{lastResult.score}</Text>
          <Text style={styles.resultTime}>{lastResult.time}</Text>
          <Text style={styles.resultMessage}>{lastResult.message}</Text>
        </View>
      ) : (
        <Text style={styles.emptyState}>No finished rounds yet.</Text>
      )}

      <Text style={styles.sectionHeader}>History</Text>
      <ScrollView style={styles.historyList}>
        {pastGames.map(game => (
          <View key={game.id} style={styles.historyCard}>
            <View>
              <Text style={styles.historyDate}>{game.date} - {game.score}</Text>
              <Text style={styles.historyDetail}>{game.time}</Text>
            </View>
            <Text style={[styles.statusText, game.result === 'Won' ? styles.won : styles.lost]}>
              {game.result}
            </Text>
          </View>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: '#131F24' },
  title: { fontSize: 28, fontWeight: 'bold', color: '#FFF', textAlign: 'center', marginVertical: 20 },
  sectionHeader: { fontSize: 18, color: '#FFF', fontWeight: 'bold', marginTop: 24, marginBottom: 10 },
  resultCard: {
    backgroundColor: '#202F36',
    borderRadius: 14,
    padding: 20,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#2F3F47',
  },
  resultStatus: { fontSize: 20, fontWeight: '800', marginBottom: 8 },
  resultScore: { color: '#FFF', fontSize: 28, fontWeight: 'bold', marginBottom: 4 },
  resultTime: { color: '#B0B0B0', fontSize: 16, marginBottom: 12 },
  resultMessage: { color: '#D9E5EB', fontSize: 15 },
  emptyState: { color: '#B0B0B0', fontSize: 15, marginBottom: 14 },
  historyList: { flex: 1 },
  historyCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    backgroundColor: '#202F36',
    padding: 15,
    borderRadius: 10,
    marginBottom: 10,
    alignItems: 'center',
  },
  historyDate: { color: '#FFF', fontSize: 16, fontWeight: '600' },
  historyDetail: { color: '#808080', fontSize: 13, marginTop: 2 },
  statusText: { fontSize: 16, fontWeight: 'bold' },
  won: { color: '#58CC02' },
  lost: { color: '#EA2B2B' },
});
