import React, {useEffect, useState} from "react";
import styles from "./LeaderboardPanel.module.css";
import {getPlayerStats, getTopPlayers} from "../../api/leaderboardApi.js";

export const LeaderboardPanel = ({leaderboard: topPlayers, playerStats: curPlayerStats}) => {
    if (!topPlayers) return;

    const [leaderboard, setLeaderboard] = useState(topPlayers);
    const [playerStats, setPlayerStats] = useState(curPlayerStats);

    const [sortBy, setSortBy] = useState("bestScore");
    const [sortType, setSortType] = useState("DESC");

    const pageSize = 10;
    const [page, setPage] = useState(1);

    const columns = ['#', 'Player', 'Games', 'Wins', 'Win ratio', 'Total score', 'Best score'];
    const statNames = ['player', 'gamesPlayed', 'gamesWon', 'winRatio', 'totalScore', 'bestScore'];

    console.log(leaderboard);

    const getRowClassName = (index) => {
        if (index === 0) return "firstPlaceRow";
        if (index === 1) return "secondPlaceRow";
        if (index === 2) return "thirdPlaceRow";

        return "";
    }

    const getColumnArrow = () => sortType === "DESC" ? "↓" : "↑";

    const getPageNum = (index) => {
        const visiblePageCount = 7;
        const pagesAroundCurrent = 3;
        const totalPages = 10;

        const maxStartPage = Math.max(1, totalPages - visiblePageCount + 1);

        const baseStartPage = page - pagesAroundCurrent;

        const startPage = Math.min(
            Math.max(1, baseStartPage),
            maxStartPage
        );

        return startPage + index;
    }

    if (playerStats)
        playerStats.winRatio = (playerStats.gamesWon / playerStats.gamesPlayed) * 100;

    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const visiblePlayers = leaderboard.slice(startIndex, endIndex);

    useEffect(() => {
        const updateLeaderboard = async () => {
            const leaderboard = await getTopPlayers({sortBy: sortBy, sortType: sortType});
            const playerStats = await getPlayerStats("ThatsIt")

            console.log(playerStats);
            setLeaderboard(leaderboard);
            setPlayerStats(playerStats);
        };
        updateLeaderboard();
    }, [sortBy, sortType]);

    return (
        <div className={styles.leaderboardPanel}>
            <div className={styles.leaderboardHeader}>
                <div>
                    <p className={styles.leaderboardEyebrow}>Leaderboard</p>
                    <h1 className={styles.leaderboardTitle}>Top 100 players</h1>
                </div>

                <span className={styles.leaderboardPill}>
                    Best score ↓
                </span>
            </div>

            <section className={styles.currentPlayerCard}>
                <div className={styles.currentPlayerTop}>
                    <div>
                        <p className={styles.currentPlayerLabel}>Your stats</p>
                        <h2 className={styles.currentPlayerName}>
                            {playerStats ? playerStats.player : "No games yet"}
                        </h2>
                    </div>

                    <div className={styles.currentRankBadge}>
                        {playerStats ? `#${playerStats.rank}` : "—"}
                    </div>
                </div>

                    <div className={styles.currentStatsGrid}>
                        {playerStats &&
                            columns.map((column, index) => {
                                if (index <= 1) return;
                                return (
                                    <div key={`${playerStats.ident}-${column}`} className={styles.statItem}>
                                        <span className={styles.statLabel}>{column}</span>
                                        <strong className={styles.statValue}>
                                            {`${playerStats[statNames[index - 1]]}${column === "Win ratio" ? "%" : ""}`}
                                        </strong>
                                    </div>
                                );
                            })
                        }
                    </div>
            </section>

            <section className={styles.tableCard}>
                <div className="table-responsive">
                    <table className={styles.leaderboardTable}>
                        <thead>
                        <tr>
                            {columns.map(((column, index) => {
                                const isSortable = index >= 2;
                                const sortName = statNames[index - 1];
                                const isButtonActive = isSortable && sortBy === sortName;

                                return (
                                    <th scope="col">
                                        <button
                                            type="button"
                                            disabled={!isSortable}
                                            onClick={() => {
                                                if (!isSortable) return;

                                                setSortBy(statNames[index - 1]);
                                                setSortType(sortType === "DESC" ? "ASC" : "DESC");
                                            }}
                                            className={`
                                                ${styles.sortButton}
                                                ${isButtonActive ? styles.sortButtonActive : ""}
                                            `}
                                        >
                                            {`${column} ${isButtonActive ? getColumnArrow(index) : ""}`}
                                        </button>
                                    </th>
                                );
                            }))}
                        </tr>
                        </thead>

                        <tbody>
                        {visiblePlayers?.map(((player, index) => {
                            const playerName = player.player;
                            const winRatio = Math.round((player.gamesWon / player.gamesPlayed) * 100);
                            return (
                                <tr
                                    key={player.ident}
                                    className={getRowClassName(index)}
                                >
                                    <td className={styles.rankCell}>{(page - 1) * pageSize + (index + 1)}</td>
                                    <td>
                                        <div className={styles.playerCell}>
                                            <span className={styles.playerAvatar}>
                                                {playerName.toUpperCase().charAt(0)}
                                            </span>
                                            <strong className={styles.playerName}>{playerName}</strong>
                                        </div>
                                    </td>
                                    {statNames.map((statName, idx) => {
                                        if (idx === 0) return;
                                        return (
                                            <td
                                                key={`${player.ident}-${statName}`}
                                                className={styles.numericCell}
                                            >
                                                {
                                                    statName === 'winRatio'
                                                    ? `${winRatio}%`
                                                    : player[statName]
                                                }
                                            </td>
                                        );
                                    })}

                                </tr>
                            );
                        }))}
                        </tbody>
                    </table>
                </div>

                <div className={styles.paginationRow}>
                    <p className={styles.paginationText}>
                        {`Showing ${startIndex + 1}–${endIndex} of 100 players`}
                    </p>

                    <div className="d-flex gap-2">
                        {Array.from({length: 7}).map((_, index) => {
                            const pageNum = getPageNum(index);
                            return (
                                <button
                                    key={pageNum}
                                    type="button"
                                    className={`
                                        btn
                                        ${styles.pageButton}
                                        ${page === (pageNum) ? styles.pageButtonActive : ""}`
                                    }
                                    onClick={() => {
                                        console.log(pageNum);
                                        setPage(pageNum);
                                    }}
                                >
                                    {pageNum}
                                </button>
                            );
                        })}
                    </div>
                </div>
            </section>
        </div>
    );
};