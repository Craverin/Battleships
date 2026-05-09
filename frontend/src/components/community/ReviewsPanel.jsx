import React, {useEffect, useRef, useState} from "react";
import styles from "./ReviewsPanel.module.css";
import {addComment, getComments, getPlayerComments, getRatingSummary, setRating} from "../../api/reviewsApi.js";

export const ReviewsPanel = ({playerComments: userComments, comments: allComments, playerRating: rating, ratingSummary: ratingSum}) => {
    if (!userComments || !allComments || !ratingSum || !rating) return;

    const pageSize = 10;
    const totalComments = allComments.length;
    const totalPages = Math.ceil(totalComments/ pageSize);
    const visiblePageCount = 5;
    const pagesAroundCurrent = (visiblePageCount - 1) / 2;

    const [page, setPage] = useState(1);

    const [comments, setComments] = useState(allComments);
    const [playerComments, setPlayerComments] = useState(userComments);
    const [playerComment, setPlayerComment] = useState("");
    const [playerRating, setPlayerRating] = useState(rating);
    const [ratingSummary, setRatingSummary] = useState(ratingSum);

    const startIndex = (page - 1) * pageSize;
    let endIndex = startIndex + pageSize;

    const visiblePlayerComments = playerComments.slice(startIndex, endIndex);
    endIndex = (endIndex - visiblePlayerComments.length) <= 0 ? 0 : endIndex - visiblePlayerComments.length;
    const visibleComments = comments.slice(startIndex, endIndex);

    const starsActive = playerRating === -1 ? 0 : playerRating;
    const [starButtonsActive, setStarButtonsActive] = useState({starsActive})

    const getReviewDate = (dateString) => {
        const date = new Date(dateString);

        const datePart = new Intl.DateTimeFormat("en-US", {
            month: "short",
            day: "numeric",
            year: "numeric",
        }).format(date);

        const timePart = new Intl.DateTimeFormat("en-US", {
            hour: "2-digit",
            minute: "2-digit",
            hour12: false,
        }).format(date);

        return `${datePart} · ${timePart}`;
    };

    const getPageNum = (index) => {
        const maxStartPage = Math.max(1, totalPages - visiblePageCount + 1);

        const baseStartPage = page - pagesAroundCurrent;

        const startPage = Math.min(
            Math.max(1, baseStartPage),
            maxStartPage
        );

        return startPage + index;
    };

    const showPlayerComment = (comment, isPlayerComment = false) => {
        return (
            <section className={`${styles.reviewCard} ${isPlayerComment ? styles.reviewCardOwn : ""}`}>
                <div className={styles.reviewTop}>
                    <div className={styles.reviewerInfo}>
                       <span className={styles.reviewerAvatar}>
                           {comment.player.toUpperCase().charAt(0)}
                       </span>

                        <div>
                            <h3 className={styles.reviewerName}>
                                {comment.player}
                            </h3>

                            <p className={styles.reviewDate}>
                                {getReviewDate(comment.commentedOn)}
                            </p>
                        </div>
                    </div>
                </div>

                <p className={styles.reviewComment}>
                    {comment.comment}
                </p>
            </section>
        );
    }

    const addNewComment = async (comment) => {
        await addComment("NavalAce_054", "battleships", comment, new Date());
        const newPlayerComments = await getPlayerComments('NavalAce_054');
        const newComments = await getComments();

        setPlayerComments(newPlayerComments);
        setComments(newComments);
    }

    const commentsTopRef = useRef(null);
    const isFirstRenderRef = useRef(true);

    useEffect(() => {
        if (isFirstRenderRef.current)
        {
            isFirstRenderRef.current = false;
            return;
        }

        commentsTopRef.current?.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }, [page]);

    useEffect(() => {
        const updateRatingSummary = async () => {
            setRatingSummary(await getRatingSummary());
        }
        updateRatingSummary();
    }, [playerRating]);

    return (
        <div className={styles.reviewsPanel}>
            <div className={styles.reviewsHeader}>
                <div>
                    <p className={styles.reviewsEyebrow}>Reviews</p>
                </div>
            </div>

            <div className={styles.reviewsTopGrid}>
                <section className={styles.reviewFormCard}>
                    <div className={styles.cardHeader}>
                        <div>
                            <p className={styles.cardLabel}>Your review</p>
                            <h2 className={styles.cardTitle}>Leave feedback</h2>
                        </div>
                    </div>

                    <div className={styles.ratingPicker}>
                        {Array.from({length: 5}).map((_, index) => {
                            const starCount = index + 1;
                            return (
                                <button
                                    key={starCount}
                                    type="button"
                                    className={`
                                        ${styles.starButton}
                                        ${starButtonsActive.starsActive >= starCount ? styles.starButtonActive : ""}
                                     `}
                                    onMouseEnter={() => setStarButtonsActive({starsActive: starCount})}
                                    onMouseLeave={() => setStarButtonsActive({starsActive})}
                                    onClick = {async () => {
                                        await setRating('NavalAce_054', 'battleships', starCount, new Date());
                                        setPlayerRating(starCount);
                                    }}
                                >
                                    ★
                                </button>
                            )
                        })}

                        <span
                            className={`
                                ${styles.ratingHint}
                                ${playerRating > 0 ? "" : styles.ratingHintEmpty}
                            `}
                        >
                            {
                                playerRating > 0
                                ? `Your rating: ${playerRating} / 5`
                                : "No rating yet"
                            }
                        </span>
                    </div>

                    <label className={styles.inputLabel}>
                        Comment
                    </label>

                    <textarea
                        value={playerComment}
                        onChange={(event) => setPlayerComment(event.target.value)}
                        className={`form-control ${styles.reviewTextarea}`}
                        placeholder="Share your thoughts about the game..."
                        rows={5}
                    />

                    <div className={styles.formFooter}>
                        <button
                            type="button"
                            className={`btn ${styles.submitCommentButton}`}
                            disabled={playerComment?.trim().length === 0}
                            onClick={() => addNewComment(playerComment)}
                        >
                            Submit comment
                        </button>
                    </div>
                </section>

                <section className={styles.ratingSummaryCard}>
                    <p className={styles.cardLabel}>Average rating</p>

                    <div className={styles.averageRating}>
                        <strong className={styles.averageValue}>{ratingSummary.averageRating}</strong>
                        <span className={styles.averageMax}>/ 5</span>
                    </div>

                    <div className={styles.summaryStars}>
                        ★★★★★
                    </div>

                    <p className={styles.summaryText}>
                        {`Based on ${ratingSummary.totalRatings} player reviews.`}
                    </p>

                    <div className={styles.ratingBreakdown}>
                        {ratingSummary?.ratingDistribution.map(rating => {
                            const width =
                                rating.percent === 0 && rating.count > 0
                                ? '0.45%'
                                : `${rating.percent}%`;
                            const ratingPercent = rating.percent === 0 && rating.count > 0 ? "<1" : rating.percent;

                            return (
                                <div key={rating.rating} className={styles.ratingRow}>
                                    <span className={styles.ratingRowLabel}>
                                        {`${rating.rating} stars`}
                                    </span>
                                    <div className={styles.ratingTrack}>
                                        <div
                                            className={styles.ratingFill}
                                            style={{ width: width }}
                                        />
                                    </div>
                                    <span className={styles.ratingPercent}>
                                        {`${ratingPercent}%`}
                                    </span>
                                </div>
                            );
                        })}
                    </div>
                </section>
            </div>

            <section ref={commentsTopRef} className={styles.reviewsListCard}>
                <div className={styles.listHeader}>
                    <div>
                        <p className={styles.cardLabel}>Community</p>
                        <h2 className={styles.cardTitle}>Latest comments</h2>
                    </div>

                    <span className={styles.listBadge}>
                        {`${comments.length} total`}
                    </span>
                </div>

                <div className={styles.reviewsList}>
                    {visiblePlayerComments && visiblePlayerComments.map(comment => showPlayerComment(comment, true))}
                    {visibleComments && visibleComments.map(comment => showPlayerComment(comment))}
                </div>

                <div className={styles.commentsPaginationRow}>
                    <p className={styles.commentsPaginationText}>
                        {`Showing ${startIndex + 1} – ${(startIndex + 10) > totalComments
                                                        ? totalComments
                                                        : startIndex + 10} of ${totalComments} comments`}
                    </p>

                    <div className="d-flex align-items-center gap-2 flex-wrap">
                        <button
                            type="button"
                            className={`btn ${styles.commentsPageButton}`}
                            disabled={page <= (pagesAroundCurrent + 1)}
                            onClick={() => setPage(1)}
                        >
                            {'<<'}
                        </button>

                        <button
                            type="button"
                            className={`btn ${styles.commentsPageButton}`}
                            disabled={page === 1}
                            onClick={() => setPage(page - 1)}
                        >
                            {'<'}
                        </button>

                        {Array.from({length: 5}).map((_, index) => {
                            const pageNum = getPageNum(index);
                            return (
                                <button
                                    type="button"
                                    className={`
                                        btn
                                        ${styles.commentsPageButton}
                                        ${pageNum === page ? styles.commentsPageButtonActive : ""}
                                    `}
                                    onClick={() => setPage(pageNum)}
                                >
                                    {pageNum}
                                </button>
                            )
                        })}

                        <button
                            type="button"
                            className={`btn ${styles.commentsPageButton}`}
                            disabled={page === totalPages}
                            onClick={() => setPage(page + 1)}
                        >
                            {'>'}
                        </button>

                        <button
                            type="button"
                            className={`btn ${styles.commentsPageButton}`}
                            disabled={page >= (totalPages - pagesAroundCurrent)}
                            onClick={() => setPage(totalPages)}
                        >
                            {'>>'}
                        </button>
                    </div>
                </div>
            </section>
        </div>
    );
};