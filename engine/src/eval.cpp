#include "eval.hpp"

int Eval::evaluate(const Board& board) {
    int score = 0;
    for (int i = 0; i < 64; ++i) {
        Piece p = board.get_piece(i);
        if (p.type == PieceType::NONE) continue;

        int value = 0;
        switch (p.type) {
            case PieceType::PAWN:   value = 100; break;
            case PieceType::KNIGHT: value = 320; break;
            case PieceType::BISHOP: value = 330; break;
            case PieceType::ROOK:   value = 500; break;
            case PieceType::QUEEN:  value = 900; break;
            case PieceType::KING:   value = 20000; break;
            default: break;
        }

        if (p.color == Color::WHITE) {
            score += value;
        } else {
            score -= value;
        }
    }
    return score;
}