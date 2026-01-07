package `in`.co.washing_machine.mushroomscanner

/**
 * 自动生成的强力纠错集 (Force Correction) - Top 5 版
 * 阈值: 0.6
 */
object ConfusionData {
    val CONFUSION_SETS: Map<Pair<Char, Char>, Double> = mapOf(
        Pair('茵', '菌') to 0.1,
        Pair('姑', '菇') to 0.2,
        Pair('手', '毛') to 0.3, Pair('毛', '手') to 0.3,
        Pair('曰', '日') to 0.1,
        Pair('末', '未') to 0.1,
        Pair('士', '土') to 0.1,
        Pair('全', '金') to 0.1,
        Pair('大', '太') to 0.2, Pair('太', '大') to 0.2,
        Pair('前', '茄') to 0.2,
        Pair('热', '荪') to 0.1,
        Pair('苏', '荪') to 0.1,
        Pair('莎', '荪') to 0.1,
        Pair('款', '荪') to 0.3,
        Pair('赫', '荪') to 0.1,
        Pair('恭', '荪') to 0.2,
        Pair('蒜', '荪') to 0.2,
        Pair('持', '荪') to 0.5,
        Pair('获', '荪') to 0.2,
        Pair('艺', '芝') to 0.1,
        Pair('采', '菜') to 0.1,
        Pair('康', '庸') to 0.1, Pair('庸', '康') to 0.1,
        Pair('唐', '庸') to 0.1,
        Pair('庙', '庸') to 0.1,
        Pair('葛', '蔫') to 0.1,
        Pair('莓', '蔫') to 0.1,
        Pair('瓜', '爪') to 0.1,
        Pair('姿', '凑') to 0.1,
        Pair('决', '凑') to 0.1,
        Pair('淡', '凑') to 0.1, Pair('凑', '淡') to 0.1,
        Pair('凌', '凑') to 0.1,
        Pair('漆', '凑') to 0.1,
        Pair('股', '般') to 0.1,
        Pair('贸', '贾') to 0.1,
        Pair('训', '诩') to 0.1,
        Pair('谢', '诩') to 0.1,
        Pair('翊', '诩') to 0.1,
        Pair('果', '巢') to 0.1,
        Pair('薪', '癖') to 0.2,
        Pair('病', '癖') to 0.1,
        Pair('进', '逃') to 0.1,
        Pair('械', '椰') to 0.1,
        Pair('棚', '椰') to 0.1,
        Pair('螺', '螈') to 0.1,
        Pair('倾', '硕') to 0.1,
        Pair('陶', '幽') to 0.3,
        Pair('腾', '膨') to 0.3,
        Pair('座', '鹿') to 0.2,
        Pair('部', '郃') to 0.1,
        Pair('刷', '瑚') to 0.3,
        Pair('带', '滞') to 0.3,
        Pair('爸', '鸢') to 0.4,
        Pair('彦', '鸢') to 0.4,
        Pair('仙', '袖') to 0.4,
        Pair('做', '傲') to 0.1,
        Pair('前', '萌') to 0.2,
    )

    val LOW_COST_MISSING_CHARS: Map<Char, Double> = mapOf(
        '一' to 0.2,
    )
}
