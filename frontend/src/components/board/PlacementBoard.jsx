import React, {useEffect, useState} from "react";
import boardStyles from "./Board.module.css";
import styles from "./PlacementBoard.module.css";
import {moveShip} from "../../api/gameApi.js";
import {BOARD_SIZE} from "../../pages/game/GamePage.jsx";

const CELL_SIZE = 34;
const CELL_GAP = 4;
const BOARD_PADDING = 12;
const GRID_STEP = CELL_SIZE + CELL_GAP;

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

    const offsetX = parseFloat(event.dataTransfer.getData("offsetX"));
    const offsetY = parseFloat(event.dataTransfer.getData("offsetY"));

    const x = event.clientX - board.left - BOARD_PADDING - offsetX;
    const y = event.clientY - board.top - BOARD_PADDING - offsetY;

    const gridCol = Math.floor(x / GRID_STEP);
    const gridRow = Math.floor(y / GRID_STEP);

    return {
        row: gridRow - 1,
        col: gridCol - 1
    };
}

const handleDragStart = (event, shipId) => {
    const ship = event.currentTarget.getBoundingClientRect();

    const dragOffsetX = Math.floor(event.clientX - ship.left);
    const dragOffsetY = Math.floor(event.clientY - ship.top);

    event.dataTransfer.setData("shipId", shipId);
    event.dataTransfer.setData("offsetX", dragOffsetX.toString());
    event.dataTransfer.setData("offsetY", dragOffsetY.toString());
}

const isValidPlacement = (row, col, length, orientation) => {
    if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE)
        return false;

    const endRow = orientation === "VERTICAL" ? row + length - 1 : row;
    const endCol = orientation === "HORIZONTAL" ? col + length - 1 : col;

    return endRow < BOARD_SIZE && endCol < BOARD_SIZE;
};

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

const canPlaceShip = (ships, shipId, row, col, length, orientation) => {
    const movingShipCoords = getOccupiedCoordinates(row, col, length, orientation);

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

export const PlacementBoard = ({gameId, playerToken, ships: boardShips = [], isLocked = false}) => {
    const [ships, setShips] = useState(boardShips);
    const gridSize = BOARD_SIZE + 1;

    const handleMoveShip = async (shipId, row, col, orientation) => {
        if (!gameId) return;

        const resp = await moveShip(gameId, shipId, playerToken, {row, col, orientation});
        setShips(resp.ships);
    }

    const handleBoardDrop = (event) => {
        event.preventDefault();

        const shipId = event.dataTransfer.getData("shipId");
        const ship = ships.find(ship => String(ship.shipId) === String(shipId));

        if (!ship) return;

        const {row, col} = getDroppedCellCoordinates(event, event.currentTarget);
        console.log(`(row, col) = (${row}, ${col})`);

        if (isValidPlacement(row, col, ship.length, ship.orientation) && canPlaceShip(ships, shipId, row, col, ship.length, ship.orientation))
            handleMoveShip(shipId, row, col, ship.orientation);
    }

    const handleRotation = (event, ship) => {
        const shipRect = event.currentTarget.getBoundingClientRect();

        const offset = ship.orientation === "HORIZONTAL"
                               ? event.clientX - shipRect.left
                               : event.clientY - shipRect.top;

        const index = Math.floor(offset / GRID_STEP);

        const newOrientation = ship.orientation === "HORIZONTAL" ? "VERTICAL" : "HORIZONTAL";

        let newRow;
        let newCol;

        if (ship.orientation === "HORIZONTAL")
        {
            newRow = ship.row - index;
            newCol = ship.col + index;
        }

        else
        {
            newRow = ship.row + index;
            newCol = ship.col - index;
        }

        console.log(`${newRow} ${newCol}`);
        if (isValidPlacement(newRow, newCol, ship.length, newOrientation)
            && canPlaceShip(ships, ship.shipId, newRow, newCol, ship.length, newOrientation))
        {
            handleMoveShip(ship.shipId, newRow, newCol, newOrientation);
        }
    }

    useEffect(() => {
        if (!boardShips) return;
        setShips(boardShips);
    }, [boardShips]);

    return (
        <div className={`card border-0 shadow-lg ${boardStyles.boardCard}`}>
            <div className="card-body p-3 p-md-4">
                <div className={boardStyles.boardViewport}>
                    <div
                        className={boardStyles.board}
                        onDragOver={event => event.preventDefault()}
                        onDrop={event => handleBoardDrop(event)}
                    >
                        <div className={boardStyles.grid}>
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
                                            boardStyles.cell,
                                            isMarkup ? boardStyles.markupCell : styles.playCell,
                                            isCorner ? boardStyles.cornerCell : ""
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
                                        onDragStart={event => handleDragStart(event, ship.shipId)}
                                        onClick={event => {
                                            handleRotation(event, ship);
                                        }}
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