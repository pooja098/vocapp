import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Modal,
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

import ResultsScreen, { PastGame, ResultSummary } from './results';
import SettingsScreen, { updateRoundTime, updateTotalPairs } from './settings';
import { INITIAL_WORD_POOL, type WordPair } from './words';

interface ActiveWord {
  id: string;
  word: string;
}

type ScreenType = 'Puzzle' | 'Settings' | 'Results';

const STORAGE_KEY = 'vocapp.top-results.v1';
const MAX_STORED_RESULTS = 10;

const DEFAULT_PAST_GAMES: PastGame[] = [
  { id: 1, date: 'Today', result: 'Won', score: '10/10', time: '32s left', elapsedSeconds: 13 },
  { id: 2, date: 'Yesterday', result: 'Lost', score: '4/10', time: '0s left', elapsedSeconds: 45 },
];

export default function App(): React.JSX.Element {
  const [currentScreen, setCurrentScreen] = useState<ScreenType>('Puzzle');
  const [roundTime, setRoundTime] = useState<number>(45);
  const [totalPairsToWin, setTotalPairsToWin] = useState<number>(10);
  const [pastGames, setPastGames] = useState<PastGame[]>(DEFAULT_PAST_GAMES);

  const [timeLeft, setTimeLeft] = useState<number>(roundTime);
  const [gameActive, setGameActive] = useState<boolean>(false);
  const [gamePool, setGamePool] = useState<WordPair[]>([]);
  const [activeDeWords, setActiveDeWords] = useState<ActiveWord[]>([]);
  const [activeEnWords, setActiveEnWords] = useState<ActiveWord[]>([]);
  const [selectedDe, setSelectedDe] = useState<ActiveWord | null>(null);
  const [selectedEn, setSelectedEn] = useState<ActiveWord | null>(null);
  const [matchedPairsCount, setMatchedPairsCount] = useState<number>(0);
  const [lastResult, setLastResult] = useState<ResultSummary | null>(null);
  const [resultDialogVisible, setResultDialogVisible] = useState<boolean>(false);

  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const persistPastGames = useCallback(async (nextGames: PastGame[]): Promise<void> => {
    const trimmed = [...nextGames]
      .sort((a, b) => {
        const aDuration = a.elapsedSeconds ?? Number.POSITIVE_INFINITY;
        const bDuration = b.elapsedSeconds ?? Number.POSITIVE_INFINITY;
        if (a.result !== b.result) {
          return a.result === 'Won' ? -1 : 1;
        }
        return aDuration - bDuration;
      })
      .slice(0, MAX_STORED_RESULTS);

    setPastGames(trimmed);
    await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(trimmed));
  }, []);

  const resetSelectionState = useCallback((): void => {
    setSelectedDe(null);
    setSelectedEn(null);
  }, []);

  useEffect(() => {
    const loadSavedGames = async (): Promise<void> => {
      try {
        const rawValue = await AsyncStorage.getItem(STORAGE_KEY);
        if (!rawValue) {
          return;
        }

        const savedGames = JSON.parse(rawValue) as PastGame[];
        if (Array.isArray(savedGames) && savedGames.length > 0) {
          setPastGames(savedGames.slice(0, MAX_STORED_RESULTS));
        }
      } catch (error) {
        console.warn('Failed to load saved game results', error);
      }
    };

    void loadSavedGames();
  }, []);

  const finishRound = useCallback(
    (isWin: boolean, finalMatchedCount: number): void => {
      if (!gameActive) {
        return;
      }

      setGameActive(false);
      if (timerRef.current) {
        clearTimeout(timerRef.current);
        timerRef.current = null;
      }

      const elapsedSeconds = Math.max(0, roundTime - timeLeft);
      const score = `${finalMatchedCount}/${totalPairsToWin}`;
      const newRecord: PastGame = {
        id: Date.now(),
        date: 'Just Now',
        result: isWin ? 'Won' : 'Lost',
        score,
        time: isWin ? `${elapsedSeconds}s used` : 'Timed Out',
        elapsedSeconds,
      };

      const summary: ResultSummary = {
        isWin,
        score,
        time: isWin ? `${elapsedSeconds}s used` : 'Timed Out',
        message: isWin ? 'All pairs matched successfully.' : 'Time ran out before the round was complete.',
      };

      setPastGames(prevGames => {
        const nextGames = [newRecord, ...prevGames];
        void persistPastGames(nextGames);
        return nextGames;
      });
      setLastResult(summary);
      setResultDialogVisible(true);
      setCurrentScreen('Results');
    },
    [gameActive, roundTime, timeLeft, totalPairsToWin, persistPastGames]
  );

  const startNewGame = useCallback((): void => {
    const shuffledPool = [...INITIAL_WORD_POOL].sort(() => 0.5 - Math.random());
    const roundPool = shuffledPool.slice(0, Math.min(totalPairsToWin, INITIAL_WORD_POOL.length));

    const initialDe: ActiveWord[] = roundPool.slice(0, 5).map(item => ({ id: item.id, word: item.de }));
    const initialEn: ActiveWord[] = roundPool.slice(0, 5).map(item => ({ id: item.id, word: item.en }));

    setGamePool(roundPool.slice(5));
    setActiveDeWords(initialDe.sort(() => 0.5 - Math.random()));
    setActiveEnWords(initialEn.sort(() => 0.5 - Math.random()));
    setMatchedPairsCount(0);
    setTimeLeft(roundTime);
    resetSelectionState();
    setResultDialogVisible(false);
    setLastResult(null);
    setGameActive(true);
    setCurrentScreen('Puzzle');
  }, [resetSelectionState, roundTime, totalPairsToWin]);

  useEffect(() => {
    if (!gameActive) {
      return;
    }

    if (timeLeft > 0) {
      timerRef.current = setTimeout(() => {
        setTimeLeft(currentTime => currentTime - 1);
      }, 1000);

      return () => {
        if (timerRef.current) {
          clearTimeout(timerRef.current);
          timerRef.current = null;
        }
      };
    }

    if (timeLeft === 0) {
      finishRound(false, matchedPairsCount);
    }
  }, [timeLeft, gameActive, matchedPairsCount, finishRound]);

  useEffect(() => {
    if (!selectedDe || !selectedEn) {
      return;
    }

    const correctId = selectedDe.id;

    if (selectedDe.id === selectedEn.id) {
      const nextMatchedCount = matchedPairsCount + 1;
      setMatchedPairsCount(nextMatchedCount);

      if (nextMatchedCount >= totalPairsToWin) {
        resetSelectionState();
        finishRound(true, nextMatchedCount);
        return;
      }

      let nextItem: WordPair | null = null;
      const updatedPool = [...gamePool];
      if (updatedPool.length > 0) {
        nextItem = updatedPool.shift() || null;
        setGamePool(updatedPool);
      }

      setActiveDeWords(prev =>
        prev
          .map(item => (item.id === correctId ? (nextItem ? { id: nextItem.id, word: nextItem.de } : null) : item))
          .filter((item): item is ActiveWord => item !== null)
      );

      setActiveEnWords(prev =>
        prev
          .map(item => (item.id === correctId ? (nextItem ? { id: nextItem.id, word: nextItem.en } : null) : item))
          .filter((item): item is ActiveWord => item !== null)
      );
    }

    resetSelectionState();
  }, [selectedDe, selectedEn, matchedPairsCount, totalPairsToWin, gamePool, resetSelectionState, finishRound]);

  const renderTabBar = (): React.JSX.Element => (
    <View style={styles.tabBar}>
      <TouchableOpacity
        style={[styles.tabButton, currentScreen === 'Puzzle' && styles.activeTabButton]}
        onPress={() => setCurrentScreen('Puzzle')}
      >
        <Text style={[styles.tabText, currentScreen === 'Puzzle' && styles.activeTabText]}>Puzzle</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={[styles.tabButton, currentScreen === 'Settings' && styles.activeTabButton]}
        onPress={() => setCurrentScreen('Settings')}
      >
        <Text style={[styles.tabText, currentScreen === 'Settings' && styles.activeTabText]}>Settings</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={[styles.tabButton, currentScreen === 'Results' && styles.activeTabButton]}
        onPress={() => setCurrentScreen('Results')}
      >
        <Text style={[styles.tabText, currentScreen === 'Results' && styles.activeTabText]}>Results</Text>
      </TouchableOpacity>
    </View>
  );

  const renderPuzzle = (): React.JSX.Element => {
    const progressPercent = totalPairsToWin === 0 ? 0 : (matchedPairsCount / totalPairsToWin) * 100;

    return (
      <View style={styles.container}>
        <View style={styles.puzzleHeader}>
          <TouchableOpacity
            onPress={() => {
              setGameActive(false);
              setCurrentScreen('Results');
            }}
          >
            <Text style={styles.closeIcon}>✕</Text>
          </TouchableOpacity>
          <View style={styles.progressBarBg}>
            <View style={[styles.progressBarFill, { width: `${progressPercent}%` }]} />
          </View>
          <Text style={styles.timerText}>⏳ {timeLeft}s</Text>
        </View>

        <Text style={styles.instructionText}>Tap the matching pairs</Text>

        <View style={styles.gridContainer}>
          <View style={styles.column}>
            {activeDeWords.map(item => (
              <TouchableOpacity
                key={`de-${item.id}`}
                style={[styles.wordCard, selectedDe?.id === item.id && styles.selectedCard]}
                onPress={() => setSelectedDe(item)}
              >
                <Text style={styles.wordText}>{item.word}</Text>
              </TouchableOpacity>
            ))}
          </View>

          <View style={styles.column}>
            {activeEnWords.map(item => (
              <TouchableOpacity
                key={`en-${item.id}`}
                style={[styles.wordCard, selectedEn?.id === item.id && styles.selectedCard]}
                onPress={() => setSelectedEn(item)}
              >
                <Text style={styles.wordText}>{item.word}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {!gameActive && (
          <TouchableOpacity style={styles.primaryButton} onPress={startNewGame}>
            <Text style={styles.buttonText}>Start Game</Text>
          </TouchableOpacity>
        )}
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="light-content" />
      {renderTabBar()}

      {currentScreen === 'Puzzle' && renderPuzzle()}
      {currentScreen === 'Settings' && (
        <SettingsScreen
          roundTime={roundTime}
          totalPairsToWin={totalPairsToWin}
          onRoundTimeChange={nextTime => updateRoundTime(nextTime, setRoundTime, setTimeLeft)}
          onTotalPairsChange={nextTotal => updateTotalPairs(nextTotal, setTotalPairsToWin)}
          onStartNewRound={startNewGame}
        />
      )}
      {currentScreen === 'Results' && <ResultsScreen lastResult={lastResult} pastGames={pastGames} />}

      <Modal transparent visible={resultDialogVisible} animationType="fade" onRequestClose={() => setResultDialogVisible(false)}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>{lastResult?.isWin ? '🎉' : '⌛ Time’s up!'}</Text>
            <Text style={styles.modalMessage}>{lastResult?.message}</Text>
            <Text style={styles.modalScore}>Score: {lastResult?.score}</Text>
            <Text style={styles.modalTime}>Time: {lastResult?.time}</Text>

            <TouchableOpacity
              style={styles.primaryButton}
              onPress={() => {
                setResultDialogVisible(false);
                setCurrentScreen('Results');
              }}
            >
              <Text style={styles.buttonText}>Close</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: '#131F24' },
  container: { flex: 1, padding: 20, backgroundColor: '#131F24' },
  title: { fontSize: 28, fontWeight: 'bold', color: '#FFF', textAlign: 'center', marginVertical: 20 },
  instructionText: { fontSize: 22, fontWeight: 'bold', color: '#FFF', marginVertical: 15 },
  label: { fontSize: 16, color: '#B0B0B0', marginTop: 25, marginBottom: 10, fontWeight: '600' },

  tabBar: {
    flexDirection: 'row',
    backgroundColor: '#202F36',
    borderBottomWidth: 1,
    borderBottomColor: '#2F3F47',
    paddingHorizontal: 12,
    paddingTop: 10,
  },
  tabButton: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
    borderRadius: 10,
    marginHorizontal: 4,
  },
  activeTabButton: { backgroundColor: '#1899D6' },
  tabText: { color: '#D0D9DE', fontWeight: '700', fontSize: 15 },
  activeTabText: { color: '#FFF' },

  primaryButton: { backgroundColor: '#58CC02', padding: 15, borderRadius: 12, alignItems: 'center', marginTop: 20 },
  buttonText: { color: '#FFF', fontSize: 18, fontWeight: 'bold' },
  secondaryButton: { borderWidth: 2, borderColor: '#3F4F56', padding: 15, borderRadius: 12, alignItems: 'center', marginTop: 12 },
  secondaryButtonText: { color: '#49C0F8', fontSize: 16, fontWeight: 'bold' },

  row: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 10 },
  configBtn: { backgroundColor: '#202F36', flex: 0.48, padding: 12, borderRadius: 8, alignItems: 'center' },
  configBtnTxt: { color: '#FFF', fontWeight: 'bold' },

  sectionHeader: { fontSize: 18, color: '#FFF', fontWeight: 'bold', marginTop: 24, marginBottom: 10 },
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

  puzzleHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginVertical: 10 },
  closeIcon: { color: '#607076', fontSize: 24, paddingHorizontal: 5 },
  progressBarBg: { flex: 1, height: 14, backgroundColor: '#202F36', borderRadius: 7, marginHorizontal: 15 },
  progressBarFill: { height: '100%', backgroundColor: '#1899D6', borderRadius: 7 },
  timerText: { color: '#FFF', fontWeight: 'bold', fontSize: 15 },

  gridContainer: { flexDirection: 'row', justifyContent: 'space-between', flex: 1, marginTop: 10 },
  column: { width: '48%' },
  wordCard: {
    backgroundColor: '#131F24',
    borderWidth: 2,
    borderColor: '#3F4F56',
    borderRadius: 12,
    paddingVertical: 18,
    paddingHorizontal: 10,
    marginBottom: 14,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 65,
  },
  selectedCard: { borderColor: '#1899D6', backgroundColor: '#132D3A' },
  wordText: { color: '#FFF', fontSize: 15, textAlign: 'center', fontWeight: '500' },

  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.55)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  modalCard: {
    width: '100%',
    backgroundColor: '#1B2A31',
    borderRadius: 16,
    padding: 24,
    borderWidth: 1,
    borderColor: '#30464E',
  },
  modalTitle: { color: '#FFF', fontSize: 24, fontWeight: '800', marginBottom: 8 },
  modalMessage: { color: '#D9E5EB', fontSize: 16, marginBottom: 12 },
  modalScore: { color: '#58CC02', fontSize: 18, fontWeight: '700', marginBottom: 6 },
  modalTime: { color: '#B0B0B0', fontSize: 15, marginBottom: 18 },
});