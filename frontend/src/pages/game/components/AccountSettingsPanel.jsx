import styles from "./AccountSettingsPanel.module.css";
import React, {useEffect, useState} from "react";
import {changePassword, changeUsername} from "../../../api/credentialsApi.js";

export const AccountSettingsPanel = ({currentUsername, setUser, redirectToHomePage}) => {
    const [username, setUsername] = useState("");
    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [repeatPassword, setRepeatPassword] = useState("");

    const [errorMessage, setErrorMessage] = useState(undefined);

    const handleUsernameChange = async (username) => {
        setErrorMessage(undefined);

        try
        {
            const user = await changeUsername(username);
            setUser(user);
            redirectToHomePage();
        }
        catch (err) { setErrorMessage(err.message) }
    }

    const handlePasswordChange = async (currentPassword, newPassword) => {
        setErrorMessage(undefined);

        try
        {
            await changePassword(currentPassword, newPassword);
            redirectToHomePage();
        }
        catch (err) { setErrorMessage(err.message) }
    }


    return (
        <div className={styles.accountSettingsPanel}>
            <div className={styles.accountSettingsCard}>
                <header className={styles.settingsHeader}>
                    <div
                        className={styles.profileAvatar}
                        aria-hidden="true"
                    >
                        {currentUsername[0]}
                    </div>

                    <div className={styles.headerCopy}>
                        <h1 className={styles.title}>
                            Account settings
                        </h1>

                    </div>
                </header>

                {errorMessage && (
                    <div className={styles.authErrorMessage} role="alert">
                        {errorMessage}
                    </div>
                )}
                <div className={styles.settingsForm}>
                    <form
                        className={styles.settingsSection}
                        onSubmit={(event) => {
                            event.preventDefault();
                            handleUsernameChange(username.trim());
                            setErrorMessage(undefined);
                        }}
                    >
                        <div className={styles.sectionHeader}>
                            <h2 className={styles.sectionTitle}>
                                Profile
                            </h2>

                            <p className={styles.sectionDescription}>
                                This name will be displayed in leaderboards,
                                reviews and game statistics.
                            </p>
                        </div>

                        <div className={styles.field}>
                            <label className={styles.label}>
                                Username
                            </label>

                            <input
                                type="text"
                                value={username}
                                onChange={(event) => setUsername(event.target.value)}
                                className={styles.input}
                                placeholder="Enter username"
                            />

                            <span className={styles.hint}>
                                Choose a unique name that other players will see.
                            </span>
                        </div>

                        <div className={styles.sectionActions}>
                            <button
                                type="submit"
                                disabled={username.trim().length < 4 || username === currentUsername}
                                className={`
                                    ${styles.settingsButton}
                                    ${styles.primaryButton}
                                `}
                            >
                                Save username
                            </button>
                        </div>
                    </form>

                    <form
                        className={styles.settingsSection}
                        onSubmit={(event) => {
                            event.preventDefault();
                            handlePasswordChange(currentPassword, newPassword);
                            setErrorMessage(undefined);
                        }}
                    >
                        <div className={styles.sectionHeader}>
                            <h2 className={styles.sectionTitle}>
                                Password
                            </h2>

                            <p className={styles.sectionDescription}>
                                Leave the current password empty if you signed
                                in with Google or GitHub and have not set a
                                password yet.
                            </p>
                        </div>

                        <div className={styles.field}>
                            <label className={styles.label}>
                                Current password
                            </label>

                            <input
                                type="password"
                                value={currentPassword}
                                onChange={(event) => {
                                    setCurrentPassword(event.target.value);
                                    setErrorMessage(undefined);
                                }}
                                className={styles.input}
                                placeholder="Enter current password"
                            />
                        </div>

                        <div className={styles.fieldRow}>
                            <div className={styles.field}>
                                <label className={styles.label}>
                                    New password
                                </label>

                                <input
                                    type="password"
                                    value={newPassword}
                                    onChange={(event) => setNewPassword(event.target.value)}
                                    className={styles.input}
                                    placeholder="At least 8 characters"
                                />
                            </div>

                            <div className={styles.field}>
                                <label className={styles.label}>
                                    Repeat password
                                </label>

                                <input
                                    type="password"
                                    value={repeatPassword}
                                    onChange={(event) => setRepeatPassword(event.target.value)}
                                    className={styles.input}
                                    placeholder="Repeat new password"
                                />
                            </div>
                        </div>

                        <div className={styles.sectionActions}>
                            <button
                                type="submit"
                                disabled={newPassword.trim().length < 8 || newPassword !== repeatPassword}
                                onChange={() => handlePasswordChange(currentPassword, newPassword)}
                                className={`
                                    ${styles.settingsButton}
                                    ${styles.primaryButton}
                                `}
                            >
                                Change password
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};