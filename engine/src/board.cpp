#include "board.hpp"

Board::Board() {
    reset();
}

void Board::reset() {
    for (int i = 0; i < 64; ++i) {
        squares[i] = Piece{PieceType::NONE, Color::NONE};
    }
    current_turn = Color::WHITE;
    setup_initial_position();
}

void Board::setup_initial_position() {
    PieceType back_rank[] = {
        PieceType::ROOK, PieceType::KNIGHT, PieceType::BISHOP, PieceType::QUEEN,
        PieceType::KING, PieceType::BISHOP, PieceType::KNIGHT, PieceType::ROOK
    };

    for (int i = 0; i < 8; ++i) {
        squares[56 + i] = Piece{back_rank[i], Color::BLACK};
        squares[48 + i] = Piece{PieceType::PAWN, Color::BLACK};
        squares[8 + i]  = Piece{PieceType::PAWN, Color::WHITE};
        squares[0 + i]  = Piece{back_rank[i], Color::WHITE};
    }
}

void Board::make_move(const Move& move) {
    squares[move.to] = squares[move.from];
    squares[move.from] = Piece{PieceType::NONE, Color::NONE};
    current_turn = (current_turn == Color::WHITE) ? Color::BLACK : Color::WHITE;
}

void Board::unmake_move(const Move& move) {
    squares[move.from] = squares[move.to];
    squares[move.to] = move.captured;
    current_turn = (current_turn == Color::WHITE) ? Color::BLACK : Color::WHITE;
}

int Board::find_king_square(Color color) const {
    for (int i = 0; i < 64; ++i) {
        if (squares[i].type == PieceType::KING && squares[i].color == color) {
            return i;
        }
    }
    return -1;
}