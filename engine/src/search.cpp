#include "search.hpp"
#include "movegen.hpp"
#include "eval.hpp"
#include <algorithm>

int Search::minimax(Board& board, int depth, int alpha, int beta, bool is_maximizing) {
    if (depth == 0) {
        return Eval::evaluate(board);
    }

    Color current_color = is_maximizing ? Color::WHITE : Color::BLACK;
    std::vector<Move> moves = MoveGen::generate_all_legal_moves(board, current_color);

    if (moves.empty()) {
        return is_maximizing ? -50000 : 50000;
    }

    if (is_maximizing) {
        int max_eval = -100000;
        for (const auto& move : moves) {
            board.make_move(move);
            int eval = minimax(board, depth - 1, alpha, beta, false);
            board.unmake_move(move);

            max_eval = std::max(max_eval, eval);
            alpha = std::max(alpha, eval);
            if (beta <= alpha) break;
        }
        return max_eval;
    } else {
        int min_eval = 100000;
        for (const auto& move : moves) {
            board.make_move(move);
            int eval = minimax(board, depth - 1, alpha, beta, true);
            board.unmake_move(move);

            min_eval = std::min(min_eval, eval);
            beta = std::min(beta, eval);
            if (beta <= alpha) break;
        }
        return min_eval;
    }
}

Move Search::find_best_move(Board& board, int depth) {
    Color side = board.get_turn();
    bool is_maximizing = (side == Color::WHITE);
    std::vector<Move> moves = MoveGen::generate_all_legal_moves(board, side);

    if (moves.empty()) return Move{-1, -1};

    Move best_move = moves[0];
    int best_val = is_maximizing ? -100000 : 100000;
    int alpha = -100000;
    int beta = 100000;

    for (const auto& move : moves) {
        board.make_move(move);
        int eval = minimax(board, depth - 1, alpha, beta, !is_maximizing);
        board.unmake_move(move);

        if (is_maximizing) {
            if (eval > best_val) {
                best_val = eval;
                best_move = move;
            }
            alpha = std::max(alpha, eval);
        } else {
            if (eval < best_val) {
                best_val = eval;
                best_move = move;
            }
            beta = std::min(beta, eval);
        }
    }

    return best_move;
}