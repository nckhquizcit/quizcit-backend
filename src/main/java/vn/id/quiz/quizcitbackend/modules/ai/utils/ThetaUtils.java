package vn.id.quiz.quizcitbackend.shared.utils;

public final class ThetaUtils {

    private static final double THETA_MIN = -4.0;
    private static final double THETA_MAX = 4.0;
    private static final double SCALE_MIN = 0.0;
    private static final double SCALE_MAX = 4.0;

    private ThetaUtils() {
    }

    /**
     * Chuyển theta_raw (logit, [-4, +4]) sang theta_scaled ([0, 4]).
     * Dùng để hiển thị cho FE — KHÔNG dùng cho tính toán IRT hay calibration.
     */
    public static double toScaled(double thetaRaw) {
        double scaled = (thetaRaw - THETA_MIN) / (THETA_MAX - THETA_MIN)
                * (SCALE_MAX - SCALE_MIN) + SCALE_MIN;
        return Math.round(Math.max(SCALE_MIN, Math.min(SCALE_MAX, scaled)) * 10000.0) / 10000.0;
    }

    /**
     * Chuyển ngược theta_scaled ([0, 4]) về theta_raw (logit, [-4, +4]).
     * Dùng khi cần raw từ giá trị đã scaled (ví dụ: đọc từ DB nếu lỡ lưu scaled).
     */
    public static double toRaw(double thetaScaled) {
        double raw = thetaScaled / (SCALE_MAX - SCALE_MIN)
                * (THETA_MAX - THETA_MIN) + THETA_MIN;
        return Math.round(Math.max(THETA_MIN, Math.min(THETA_MAX, raw)) * 10000.0) / 10000.0;
    }
}

//Ví dụ cách xài
// EstimateThetaResponse response = aiService.estimateTheta(request);

// double thetaRaw = response.getTheta(); // lưu vào DB
// double thetaScaled = ThetaUtils.toScaled(thetaRaw);
// trả FE