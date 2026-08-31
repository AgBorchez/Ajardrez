#include "board.hpp"

namespace Ajardrez
{
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
        
        history[history_index].move = move;
        history[history_index].castling = castling;
        history_index++;
        squares[move.to] = squares[move.from];
        squares[move.from] = Piece{PieceType::NONE, Color::NONE};
        current_turn = (current_turn == Color::WHITE) ? Color::BLACK : Color::WHITE;
        
    }

    bool Board::undo_move() {
        if (history_index == 0) {
            return false;
        }

        history_index--;
        Move last_move = history[history_index].move;

        castling = history[history_index].castling;

        Color moving_color = (current_turn == Color::WHITE) ? Color::BLACK : Color::WHITE;

        if (last_move.type == MoveType::PROMOTION) {
            squares[last_move.from] = Piece{PieceType::PAWN, moving_color};
        } else {
            squares[last_move.from] = squares[last_move.to];
        }
        squares[last_move.to] = last_move.captured;

        if (last_move.type == MoveType::CASTLE_KINGSIDE) {
            if (moving_color == Color::WHITE) {
                squares[7] = squares[5]; squares[5] = Piece{};
            } else {
                squares[63] = squares[61]; squares[61] = Piece{};
            }
        } else if (last_move.type == MoveType::CASTLE_QUEENSIDE) {
            if (moving_color == Color::WHITE) {
                squares[0] = squares[3]; squares[3] = Piece{};
            } else {
                squares[56] = squares[59]; squares[59] = Piece{};
            }
        }

        current_turn = moving_color;
        return true;
    }

    int Board::find_king_square(Color color) const {
        for (int i = 0; i < 64; ++i) {
            if (squares[i].type == PieceType::KING && squares[i].color == color) {
                return i;
            }
        }
        return -1;
    }

} // namespace Ajardrez