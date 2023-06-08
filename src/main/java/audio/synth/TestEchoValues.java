package audio.synth;

public class TestEchoValues {
    public static InstrumentData getTestData(){
        InstrumentData d = new InstrumentData();
        d.setVolume(new double[]{0.9});
        d.setPitch(new int[]{62, 63, 61, 58, 55, 57, 55, 54, 54, 49, 50, 50, 47, 44, 43, 42, 45, 46, 47, 47, 43, 42, 41, 39, 38, 38, 38, 39, 38, 40, 42, 42, 42, 41, 42, 43, 43, 42, 42, 42, 43, 43, 45, 47, 47, 47, 46, 46, 45, 44, 44, 45, 46, 47, 48, 48, 47, 48, 36, 37});
        d.setInstrument(InstrumentEnum.RETRO_SYNTH);
//        d.setDelayEcho(new int[]{12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 98000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 32666, 32666, 32666, 49000, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 49000, 49000, 49000, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 49000, 49000, 49000, 32666, 32666, 32666, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 32666, 32666, 49000, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 49000, 32666, 32666, 32666, 32666, 32666, 49000, 49000, 49000, 49000, 49000, 49000, 49000, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 32666, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 12250, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 12250, 12250, 12250, 12250, 16333, 16333, 16333, 16333, 16333, 16333, 16333, 12250, 12250, 12250, 12250, 12250, 12250, 16333, 16333, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250, 12250});
//        d.setFeedbackEcho(new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, -0.9, 0.12435597189695552, 0.12435597189695552, 0.11114754098360657, 0.10949648711943795, 0.11870023419203748, 0.11392271662763469, 0.113536299765808, 0.11135831381733022, 0.11030444964871197, 0.10176814988290399, 0.10819672131147541, 0.11188524590163938, 0.1076697892271663, 0.10784543325526934, 0.10963700234192042, 0.10872365339578456, 0.1038056206088993, 0.100152224824356, 0.09393442622950823, 0.09723653395784547, 0.11627634660421549, 0.10784543325526934, 0.1117096018735363, 0.12277517564402812, 0.12512880562060893, 0.13268149882903982, 0.13946135831381734, 0.14943793911007028, 0.1495784543325527, 0.1446604215456675, 0.1503512880562061, 0.14884074941451994, 0.1514929742388759, 0.17978922716627638, 0.18266978922716628, 0.19542154566744735, 0.2106674473067916, 0.21059718969555036, 0.2028688524590164, 0.2177985948477752, 0.20290398126463702, 0.20423887587822015, 0.20033957845433256, 0.18453161592505854, 0.1774355971896956, 0.19640515222482438, 0.1921545667447307, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
//        d.setDelayReverb(new int[]{289, 287, 275, 258, 247, 234, 241, 239, 238, 236, 219, 258, 256, 252, 257, 270, 267, 266, 250, 261, 250, 257, 248, 273, 119, 136, 130, 126, 130, 128, 128, 127, 125, 128, 126, 126, 131, 140, 144, 145, 141, 144, 145, 134, 134, 132, 126, 119, 117, 118, 125, 115, 119, 115, 104, 116, 111, 111, 112, 111, 108, 110, 109, 108, 113, 122, 119, 120, 123, 133, 139, 141, 143, 147, 147, 154, 158, 154, 147, 155, 146, 141, 139, 145, 143, 135, 149, 148, 145, 119, 127, 128, 123, 116, 48, 43, 38, 30, 33, 31, 30, 27, 22, 23, 23, 29, 27, 20, 19, 19, 23, 24, 19, 18, 18, 19, 21, 20, 26, 32, 30, 27, 26, 22, 20, 18, 22, 22, 20, 19, 17, 19, 18, 18, 22, 18, 16, 11, 12, 13, 12, 5, 4, 5, 7, 7, 8, 6, 5, 5, 4, 2, 4, 3, 6, 4, 4, 5, 6, 4, 16, 12, 12, 12, 11, 10, 15, 14, 14, 15, 20, 19, 20, 24, 24, 20, 21, 27, 27, 24, 26, 24, 22, 24, 24, 24, 19, 22, 19, 21, 19, 19, 16, 19, 18, 19, 20, 20, 15, 17, 20, 24, 26, 24, 24, 21, 18, 17, 25, 19, 21, 24, 21, 23, 29, 32, 28, 29, 28, 30, 29, 26, 21, 24, 24, 24, 25, 31, 26, 29, 26, 27, 40, 38, 36, 36, 35, 38, 39, 43, 39, 42, 41, 35, 40, 39, 35, 35, 32, 31, 33, 32, 35, 33, 34, 35, 34, 32, 32, 29, 24, 21, 22, 25, 24, 25, 24, 27, 25, 21, 26, 22, 22, 23, 25, 22, 27, 30, 30, 33, 34, 32, 34, 34, 33, 33, 31, 29, 31, 25, 32, 35, 38, 35, 28, 26, 21, 22, 20, 19, 17, 18, 19, 20, 19, 21, 22, 19, 20, 20, 19, 19, 19, 19, 18, 16, 16, 16, 16, 15, 16, 16, 17, 18, 17, 15, 15, 17, 16, 17, 14, 12, 9, 12, 11, 10, 5, 9, 10, 9, 7, 8, 7, 7, 6, 7, 7, 8, 9, 9, 12, 13, 15, 16, 17, 13, 15, 16, 19, 16, 17, 18, 17, 14, 15, 14, 17, 18, 19, 20, 21, 17, 18, 19, 21, 20, 20, 19, 21, 21, 21, 22, 26, 25, 24, 23, 25, 25, 29, 33, 30, 29, 30, 28, 27, 24, 23, 25, 24, 26, 25, 25, 26, 27, 28, 30, 28, 32, 33, 34, 31, 31, 26, 26, 25, 26, 26, 28, 28, 29, 28, 31, 31, 33, 32, 32, 35, 36, 36, 38, 40, 37, 36, 34, 37, 37, 38, 36, 34, 31, 31, 27, 31, 34, 35, 29, 28, 32, 35, 33, 35, 37, 37, 36, 36, 36, 36, 38, 37, 37, 34, 35, 35, 32, 33, 34, 33, 35, 35, 34, 38, 35, 37, 35, 38, 38, 38, 38, 41, 40, 39, 37, 36, 36, 33, 36, 32, 34, 36, 34, 33, 33, 31, 32, 36, 36, 33, 36, 39, 38, 39, 37, 37, 33, 36, 38, 34, 34, 34, 35, 33, 31, 31, 31, 28, 27, 27, 23, 22, 19, 23, 23, 14, 13, 12, 12, 14, 12, 12, 13, 15, 12, 12, 15, 17, 16, 15, 13, 17, 14, 16, 10, 8, 5, 5, 6, 4, 6, 4, 4, 5, 7, 6, 5, 4, 3, 6, 5, 7, 6, 6, 5, 7, 7, 8, 9, 9, 10, 9, 9, 8, 5, 5, 5, 4, 3, 3, 4, 4, 4, 4, 3, 3, 3, 5, 5, 5, 5, 5, 4, 5, 6, 4, 4, 5, 4, 3, 4, 3, 2, 1, 2, 4, 4, 7, 5, 3, 3, 4, 4, 5, 6, 5, 8, 8, 7, 9, 9, 11, 12, 13, 12, 9, 8, 7, 8, 9, 10, 10, 8, 7, 9, 9, 9, 9, 8, 8, 9, 7, 7, 6, 5, 6, 5, 7, 8, 6, 5, 5, 5, 4, 4, 3, 4, 4, 4, 3, 3, 4, 5, 6, 5, 5, 5, 5, 6, 5, 5, 3, 5, 3, 3, 3, 3, 2, 3, 4, 6, 3, 4, 4, 5, 4, 4, 4, 4, 4, 4, 5, 5, 6, 5, 5, 5, 5, 6, 5, 5, 6, 5, 5, 5, 4, 4, 4, 3, 4, 5, 6, 6, 6, 6, 6, 5, 4, 4, 4, 4, 4, 3, 4, 5, 4, 5, 4, 4, 3, 3, 2, 1, 0, 0, 0, 0, 1, 1, 1, 1, 3, 3, 4, 4, 5, 10, 13, 15, 17, 16, 17, 18, 17, 17, 17, 16, 15, 15, 16, 15, 17, 16, 16, 16, 14, 13, 14, 15, 12, 12, 13, 14, 15, 14, 17, 17, 20, 20, 20, 20, 18, 18, 20, 18, 18, 19, 19, 20, 20, 23, 22, 21, 22, 22, 22, 23, 25, 24, 25, 25, 25, 23, 27, 25, 24, 24, 25, 24, 26, 22, 21, 22, 21, 21, 21, 23, 22, 21, 23, 22, 25, 28, 24, 24, 25, 26, 29, 30, 31, 32, 31, 30, 30, 29, 29, 30, 31, 29, 30, 29, 31, 29, 28, 29, 26, 23, 27, 25, 24, 24, 25, 27, 28, 32, 32, 33, 34, 36, 37, 34, 33, 30, 415, 415, 429, 438, 441, 432, 432, 448, 454, 434, 412, 412, 396, 407, 401, 410, 424, 434, 445, 435, 426, 445, 438, 432, 436, 432, 422, 419, 410, 432, 425, 431, 428, 413, 413, 413, 414, 436, 423, 409, 395, 390, 378, 383, 388, 377, 378, 373, 367, 369, 375, 364, 365, 361, 375, 375, 378, 386, 380, 389, 396, 387, 391, 400, 407, 409, 416, 416, 422, 414, 418, 434, 431, 446, 440, 450, 450, 436, 441, 444, 448, 449, 445, 438, 434, 438, 428, 430, 428, 428, 420, 431, 450, 450, 447, 449, 438, 431, 442, 442, 445, 457, 454, 461, 467, 466, 466, 469, 463, 468, 475, 477, 483, 491, 485, 477, 474, 477, 474, 475, 484, 486, 489, 488, 489, 486, 486, 486, 479, 486, 497, 499, 502, 500, 485, 505, 510, 511, 511, 514, 513, 512, 508, 507, 511, 522, 514, 512, 505, 509, 512, 524, 525, 526, 537, 542, 534, 529});
        d.setDelayReverb(new int[]{289, 287, 275, 258, 247, 234, 241, 239, 238, 236, 219, 258, 256, 252, 257, 270, 267, 266, 250, 261, 250, 257, 248, 273, 119, 136, 130, 126, 130, 128, 128, 127, 125, 128, 126, 126, 131, 140, 144, 145, 141, 144, 145, 134, 134, 132, 126, 119, 117, 118, 125, 115, 119, 115, 104, 116, 111, 111, 112, 111, 108, 110, 109, 108, 113, 122, 119, 120, 123, 133, 139, 141, 143, 147, 147, 154, 158, 154, 147, 155, 146, 141, 139, 145, 143, 135, 149, 148, 145, 119, 127, 128, 123, 116, 48, 43, 38, 30, 33, 31, 30, 27, 22, 23, 23, 29, 27, 20, 19, 19, 23, 24, 19, 18, 18, 19, 21, 20, 26, 32, 30, 27, 26, 22, 20, 18, 22, 22, 20, 19, 17, 19, 18, 18, 22, 18, 16, 11, 12, 13, 12, 5, 4, 5, 7, 7, 8, 6, 5, 5, 4, 2, 4, 3, 6, 4, 4, 5, 6, 4, 16, 12, 12, 12, 11,  529});
//        d.setDelayReverb(new int[]{5510, 2205});
        d.setFeedbackReverb(new double[]{.9});

//        d.setFeedbackReverb(new double[]{0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6,0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6});
        d.setPan(new double[]{0});
        return d;
    }
}