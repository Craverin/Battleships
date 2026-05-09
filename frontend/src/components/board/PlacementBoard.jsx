import React, {useEffect, useState} from "react";
import styles from "./Board.module.css";
import {moveShip} from "../../api/gameApi.js";
import {BOARD_SIZE} from "../../pages/GamePage.jsx";

export const getCellLabel = (row, col) => {
    if (row === 0 && col === 0) return "";
    if (row === 0) return String.fromCharCode('A'.charCodeAt(0) + (col - 1));
    if (col === 0) return row;

    return "";
};

const getShipGridStyle = (ship) => {
    const isVertical = ship.orientation === "VERTICAL";

    return {
        "--ship-length": String(ship.length),
        gridColumn: `${ship.col + 2} / span ${isVertical ? 1 : ship.length}`,
        gridRow: `${ship.row + 2} / span ${isVertical ? ship.length : 1}`
    };
};

const getDroppedCellCoordinates = (event, boardElement) => {
    const board = boardElement.getBoundingClientRect();

    const cellSize = 34;
    const gap = 4;
    const padding = 12;
    const step = cellSize + gap;

    const offsetX = parseFloat(event.dataTransfer.getData("offsetX"));
    const offsetY = parseFloat(event.dataTransfer.getData("offsetY"));

    const x = event.clientX - board.left - padding - offsetX;
    const y = event.clientY - board.top - padding - offsetY;

    const gridCol = Math.floor(x / step);
    const gridRow = Math.floor(y / step);

    return {
        row: gridRow - 1,
        col: gridCol - 1
    };
}

const handleDragStart = (event, shipId) => {
    const ship = event.currentTarget.getBoundingClientRect();

    const dragOffsetX = Math.floor(event.clientX - ship.left);
    const dragOffsetY = Math.floor(event.clientY - ship.top);

    const cellSize = 34;
    const shipLengthX = ship.right - ship.left;
    const shipLengthY = ship.bottom - ship.top;

    const lengthX = Math.floor(shipLengthX / cellSize);
    const lengthY = Math.floor(shipLengthY / cellSize);

    event.dataTransfer.setData("shipId", shipId);
    event.dataTransfer.setData("offsetX", dragOffsetX.toString());
    event.dataTransfer.setData("offsetY", dragOffsetY.toString());
    event.dataTransfer.setData("lengthX", lengthX.toString());
    event.dataTransfer.setData("lengthY", lengthY.toString());
}

const isValidCoordinates = (row, col, shipLengthX, shipLengthY) => {
    if (row < 0 || row > 9 || col < 0 || col > 9) return false;
    if ((row + shipLengthY - 1) > 9 || (col + shipLengthX - 1) > 9) return false;

    return true;
}

const getOccupiedCoordinates = (row, col, length, orientation, includeAdjacent = false) => {
    let startRow, startCol, endRow, endCol;

    if (includeAdjacent)
    {
        startRow = row > 0 ? row - 1 : 0;
        startCol = col > 0 ? col - 1 : 0;

        if (orientation === "VERTICAL")
        {
            endRow = (row + length) < 9 ? row + length : 9;
            endCol = (col + 1) < 9 ? col + 1 : 9;
        }

        else
        {
            endRow = (row + 1) < 9 ? row + 1 : 9;
            endCol = (col + length) < 9 ? col + length : 9;
        }
    }

    else
    {
        startRow = row;
        startCol = col;
        endRow = orientation === "VERTICAL" ? startRow + length - 1 : startRow;
        endCol = orientation === "HORIZONTAL" ? startCol + length - 1 : startCol;
    }

    let coords = [];

    for (let i = startRow; i <= endRow; i++)
        for (let j = startCol; j <= endCol; j++)
            coords.push([i, j]);

    return coords;
}

const canPlaceShip = (ships, shipId, row, col, lengthX, lengthY) => {
    const orientation = lengthX === 0 ? "VERTICAL" : "HORIZONTAL";
    const movingShipCoords = getOccupiedCoordinates(row, col, lengthX === 0 ? lengthY : lengthX, orientation);

    return !ships.some(ship => {
        if (ship.shipId === shipId) return false;
        const shipCoords = getOccupiedCoordinates(ship.row, ship.col, ship.length, ship.orientation, true);

        return movingShipCoords.some(coordinate => {
            return shipCoords.some(curCoordinate =>
                coordinate[0] === curCoordinate[0] && coordinate[1] === curCoordinate[1]
            )
        })
    });
}

export const PlacementBoard = ({gameId, playerToken, ships: boardShips, isLocked = false}) => {
    if (!boardShips) return;

    const [ships, setShips] = useState(boardShips);
    const gridSize = BOARD_SIZE + 1;

    const handleBoardDrop = (event) => {
        event.preventDefault();

        const shipId = event.dataTransfer.getData("shipId");
        const lengthX = parseInt(event.dataTransfer.getData("lengthX"));
        const lengthY = parseInt(event.dataTransfer.getData("lengthY"));

        console.log(`length: ${lengthX}, ${lengthY}`);
        const {row, col} = getDroppedCellCoordinates(event, event.currentTarget);
        console.log(`(row, col) = (${row}, ${col})`);

        if (!isValidCoordinates(row, col, lengthX, lengthY) || !canPlaceShip(ships, shipId, row, col, lengthX, lengthY))
        {
            // maybe add some animation
        }

        else
        {
            const handleMoveShip = async () => {
                if (!gameId) return;
                const resp = await moveShip(gameId, shipId, playerToken, {row, col});
                setShips(resp.ships);
            }
            handleMoveShip();
        }
    }

    useEffect(() => {
        if (!boardShips) return;
        setShips(boardShips);
    }, [boardShips]);

    return (
        <div className={`card border-0 shadow-lg ${styles.boardCard}`}>
            <div className="card-body p-3 p-md-4">
                <div className={styles.boardViewport}>
                    <div
                        className={styles.board}
                        onDragOver={event => event.preventDefault()}
                        onDrop={event => handleBoardDrop(event)}
                    >
                        <div className={styles.grid}>
                            {Array.from({ length: gridSize * gridSize }).map((_, index) => {
                                const row = Math.floor(index / gridSize);
                                const col = index % gridSize;
                                const isMarkup = row === 0 || col === 0;
                                const isCorner = row === 0 && col === 0;

                                return (
                                    <div
                                        key={`${row}-${col}`}
                                        className={
                                        [
                                            styles.cell,
                                            isMarkup ? styles.markupCell : styles.playCell,
                                            isCorner ? styles.cornerCell : ""
                                        ]
                                            .filter(Boolean)
                                            .join(" ")}
                                    >
                                        {getCellLabel(row, col)}
                                    </div>
                                );
                            })}
                        </div>

                        <div className={styles.shipsLayer}>
                            {ships.map(ship => {
                                const orientationClassName = ship.orientation === "VERTICAL" ? styles.vertical : styles.horizontal;
                                return (
                                    <button
                                        key={ship.shipId}
                                        type="button"
                                        className={`${styles.ship} ${orientationClassName}`}
                                        style={getShipGridStyle(ship)}
                                        disabled={isLocked}
                                        draggable={!isLocked}
                                        onDragStart={event => handleDragStart(event, ship.shipId) }
                                    >
                                        {Array.from({length: ship.length}).map((_, index) => (
                                            <span
                                                key={`${ship.shipId}-${index}`}
                                                className={styles.shipSegment}
                                            />
                                        ))}
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                </div>
            </div>
       </div>
    );
};