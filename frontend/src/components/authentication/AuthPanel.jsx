import React, {useState} from "react";
import {login, register} from "../../api/authApi.js"
import styles from "./AuthPanel.module.css";

export const AuthPanel = ({signingUp: signUp = true, setUser, onAuthSuccess}) => {
    const [signingUp, setSigningUp] = useState(signUp);
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [repeatPassword, setRepeatPassword] = useState("");

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const getFooterText = () => {
        if (signingUp)
        {
            return (
                <p className={styles.authFooterText}>
                    Already have an account?
                    <button
                        type="button"
                        className={styles.authInlineButton}
                        onClick={() => {
                            setSigningUp(false);
                            clearInputs();
                        }}
                    >
                        Log in
                    </button>
                </p>
            )
        }

        return (
            <p className={styles.authFooterText}>
                New to Battleships?
                <button
                    type="button"
                    className={styles.authInlineButton}
                    onClick={() => {
                        setSigningUp(true);
                        clearInputs();
                    }}
                >
                    Create account
                </button>
            </p>
        )
    }

    const clearInputs = () => {
        setUsername("");
        setPassword("");
        setRepeatPassword("");
        setError("");
        setSuccess("");
    }

    const handleSubmit = async () => {
        setError("");
        setSuccess("");

        try
        {
            if (signingUp)
            {
                await register(username.trim(), password);

                setSigningUp(false);
                setPassword("");
                setRepeatPassword("");
                setSuccess("Account created. You can log in now.");
                return;
            }

            const user = await login(username.trim(), password);

            setUser(user);
            onAuthSuccess?.();
        } catch (error) { setError(error.message);}
    };

    return (
        <div className={styles.authPanel}>
            <div className={styles.authCard}>
                <div className={styles.authHeader}>
                    <p className={styles.authEyebrow}>Account</p>
                    <h1 className={styles.authTitle}>
                        {signingUp ? "Create account" : "Welcome back"}
                    </h1>
                    <p className={styles.authText}>
                        {
                            signingUp
                            ? 'Save your stats, appear in the leaderboard, and manage your reviews.'
                            : 'Log in to continue tracking your battles, ratings, and comments.'
                        }
                    </p>
                </div>

                <div className={styles.authTabs}>
                    <button
                        type="button"
                        className={`
                            btn
                            ${styles.authTabButton}
                            ${!signingUp ? styles.authTabButtonActive : ""}
                        `}
                        onClick={() => {
                            setSigningUp(false);
                            clearInputs();
                        }}
                    >
                        Log in
                    </button>

                    <button
                        type="button"
                        className={`
                            btn
                            ${styles.authTabButton}
                            ${signingUp ? styles.authTabButtonActive : ""}
                        `}
                        onClick={() => {
                            setSigningUp(true);
                            clearInputs();
                        }}
                    >
                        Sign up
                    </button>
                </div>

                {error && (
                    <div className={styles.authErrorMessage} role="alert">
                        {error}
                    </div>
                )}

                {success && (
                    <div className={styles.authSuccessMessage} role="status">
                        {success}
                    </div>
                )}

                <form
                    className={styles.authForm}
                    onSubmit={(event) => {
                        event.preventDefault();
                        handleSubmit();
                    }}
                >
                    <div className={styles.authField}>
                        <label className={styles.authLabel}>
                            Username
                        </label>

                        <input
                            type="text"
                            value={username}
                            className={`form-control ${styles.authInput}`}
                            placeholder="Enter username"
                            onChange={event => {
                                setUsername(event.target.value);
                                setError("");
                            }}
                        />
                    </div>

                    <div className={styles.authField}>
                        <label className={styles.authLabel}>
                            Password
                        </label>

                        <input
                            type="password"
                            value={password}
                            className={`form-control ${styles.authInput}`}
                            placeholder=
                            {
                                signingUp
                                ? "At least 8 characters"
                                : 'Enter password'
                            }
                            onChange={event => {
                                setPassword(event.target.value);
                                setError("");
                            }}
                        />
                    </div>

                    {signingUp && (
                        <div className={styles.authField}>
                            <label className={styles.authLabel}>
                                Repeat password
                            </label>

                            <input
                                type="password"
                                value={repeatPassword}
                                className={`form-control ${styles.authInput}`}
                                placeholder="Repeat password"
                                onChange={event => {
                                    setRepeatPassword(event.target.value);
                                    setError("");
                                }}
                            />
                        </div>
                        )
                    }

                    <button
                        type="submit"
                        className={`btn ${styles.authSubmitButton}`}
                        disabled={!username.trim() || password.trim().length < 8 || (signingUp && repeatPassword.trim() !== password.trim())}
                        onClick={handleSubmit}
                    >
                        {
                            signingUp
                            ? 'Create account'
                            : 'Log in'
                        }
                    </button>
                </form>

                {getFooterText()}
            </div>
        </div>
    );
};