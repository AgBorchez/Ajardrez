package com.chess.ui.components;

import java.util.HashSet;
import com.chess.ui.util.NotationUtils;
import java.util.Set;

public class BoardState {
    private final char[][] logicalState = new char[8][8];
    private final Set<String> validMoves = new HashSet<>();
    private String selectedSquare;

    public void setSelectedSquare(String sSq){
        selectedSquare = sSq;
    }

    public String getSelectedSquare(){
        return selectedSquare;
    }

    public void clearSelection(){
        selectedSquare = null;
        resetValidMoves();
    }

    public Set<String> getHints(){
        return validMoves;
    }

    public void applyMoveNotation(String moveStr) {
         
        if (moveStr == null || moveStr.length() < 4) return;
         
        int fromCol = moveStr.charAt(0) - 'a';
        int fromRow = 8 - Character.getNumericValue(moveStr.charAt(1));
        int toCol = moveStr.charAt(2) - 'a';
        int toRow = 8 - Character.getNumericValue(moveStr.charAt(3));

        char movingPiece = logicalState[fromRow][fromCol];

        if (moveStr.length() == 5) {
            char promo = moveStr.charAt(4);
            movingPiece = Character.isUpperCase(movingPiece) ? Character.toUpperCase(promo) : Character.toLowerCase(promo);
        }

        if (Character.toUpperCase(movingPiece) == 'K' && Math.abs(toCol - fromCol) == 2) {
            if (toCol == 6) {
                logicalState[fromRow][5] = logicalState[fromRow][7];
                logicalState[fromRow][7] = ' ';
            } else if (toCol == 2) {
                logicalState[fromRow][3] = logicalState[fromRow][0];
                logicalState[fromRow][0] = ' ';
            }
        }

        logicalState[toRow][toCol] = movingPiece;
        logicalState[fromRow][fromCol] = ' ';
    }

    public void resetState() {
        char[] backRankBlack = {'r','n','b','q','k','b','n','r'};
        char[] backRankWhite = {'R','N','B','Q','K','B','N','R'};

        for (int c = 0; c < 8; c++) {
            logicalState[0][c] = backRankBlack[c];
            logicalState[1][c] = 'p';
            for (int r = 2; r < 6; r++) logicalState[r][c] = ' ';
            logicalState[6][c] = 'P';
            logicalState[7][c] = backRankWhite[c];
        }
        resetValidMoves();
    }

    public void setLegalMoveTargets(Set<String> targets) {
        this.validMoves.clear();
        if (targets != null) {
            this.validMoves.addAll(targets);
        }
    }

    public boolean isPromotion(String fromSq, String toSq) {
        char piece = getPiece(fromSq);
        int targetRank = Character.getNumericValue(toSq.charAt(1));
        return (piece == 'P' && targetRank == 8) || (piece == 'p' && targetRank == 1);
    }   

    public void resetValidMoves() {
        this.validMoves.clear();
    }

    public char getPiece(int x, int y){
        return logicalState[x][y];
    }

    public char getPiece(String sq){
        int col = NotationUtils.toCol(sq);
        int row = NotationUtils.toRow(sq);
        
        return getPiece(row, col);
    }

    public boolean isLegalTarget(String sq){
        return validMoves.contains(sq);
    }

}
