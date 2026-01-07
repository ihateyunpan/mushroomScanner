package `in`.co.washing_machine.mushroomscanner

object MushroomData {
    // 所有的菌子数据列表
    val list = listOf(
        Mushroom(
            id = "ci0",
            name = "未能长大的刺头菌",
            starter = "CI",
            special = "BUG",
            save = false
        ),
        Mushroom(id = "ci1", name = "中等善良刺头菌", starter = "CI", wood = "BAI", light = "HUO"),
        Mushroom(
            id = "ci2",
            name = "中等邪恶刺头菌",
            starter = "CI",
            wood = "BAI",
            light = "HUO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ci3",
            name = "中等寒冰刺头菌",
            starter = "CI",
            wood = "BAI",
            light = "HUO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ci4",
            name = "袖珍邪恶刺头菌",
            starter = "CI",
            wood = "QIAN_NIU",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "NIGHT"
        ),
        Mushroom(
            id = "ci5",
            name = "中等荧光刺头菌",
            starter = "CI",
            wood = "QIAN_NIU",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ci6",
            name = "巨巨邪恶刺头菌",
            starter = "CI",
            wood = "QIAN_NIU",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "ci7", name = "中等太白金星刺头菌", starter = "CI", wood = "JIAN_CI"),
        Mushroom(
            id = "ci8",
            name = "中等魔法少女刺头菌",
            starter = "CI",
            wood = "JIAN_CI",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ci9",
            name = "袖珍小荧光刺头菌",
            starter = "CI",
            wood = "JIAN_CI",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ci10",
            name = "袖珍善良刺头菌",
            starter = "CI",
            wood = "JIAN_CI",
            light = "HUO"
        ),
        Mushroom(
            id = "ci11",
            name = "巨巨荧光刺头菌",
            starter = "CI",
            wood = "JIAN_CI",
            light = "HUO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ci12",
            name = "袖珍魔法少女刺头菌",
            starter = "CI",
            wood = "JIAN_CI",
            light = "HUO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ci13",
            name = "巨巨魔法少女刺头菌",
            starter = "CI",
            wood = "JIAN_JING",
            light = "TONG",
            humidifier = "BLUE",
            time = "NIGHT"
        ),
        Mushroom(
            id = "ci14",
            name = "巨巨善良刺头菌",
            starter = "CI",
            wood = "JIAN_JING",
            light = "TONG",
            humidifier = "BLUE",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ci15",
            name = "袖珍寒冰刺头菌",
            starter = "CI",
            wood = "JIAN_JING",
            light = "TONG",
            humidifier = "BLUE",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "ci16", name = "巨巨寒冰刺头菌", starter = "CI", wood = "LV_SONG"),
        Mushroom(
            id = "ci17",
            name = "袖珍太白金星刺头菌",
            starter = "CI",
            wood = "LV_SONG",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ci18",
            name = "巨巨太白金星刺头菌",
            starter = "CI",
            wood = "LV_SONG",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "mao0",
            name = "未能长大的猫爪菌",
            starter = "MAO",
            special = "BUG",
            save = false
        ),
        Mushroom(
            id = "mao1",
            name = "一般傲娇猫爪菌",
            starter = "MAO",
            wood = "BAI",
            light = "HUO"
        ),
        Mushroom(
            id = "mao2",
            name = "一般萌萌猫爪菌",
            starter = "MAO",
            wood = "BAI",
            light = "HUO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "mao3",
            name = "一般暗黑猫爪菌",
            starter = "MAO",
            wood = "BAI",
            light = "HUO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "mao4",
            name = "迷你萌萌猫爪菌",
            starter = "MAO",
            wood = "QIAN_NIU",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "mao5",
            name = "一般凑凑猫爪菌",
            starter = "MAO",
            wood = "QIAN_NIU",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "mao6",
            name = "巨大萌萌猫爪菌",
            starter = "MAO",
            wood = "QIAN_NIU",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "mao7",
            name = "一般撞色猫爪菌",
            starter = "MAO",
            wood = "JIAN_CI",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "mao8",
            name = "一般红温猫爪菌",
            starter = "MAO",
            wood = "JIAN_CI",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "mao9",
            name = "迷你凑凑猫爪菌",
            starter = "MAO",
            wood = "JIAN_CI",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "mao10",
            name = "迷你傲娇猫爪菌",
            starter = "MAO",
            wood = "JIAN_CI",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "mao11",
            name = "巨大凑凑猫爪菌",
            starter = "MAO",
            wood = "JIAN_CI",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "mao12",
            name = "迷你红温猫爪菌",
            starter = "MAO",
            wood = "JIAN_CI",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "mao13",
            name = "巨大红温猫爪菌",
            starter = "MAO",
            wood = "LV_SONG",
            light = "YU_RONG",
            humidifier = "TAO"
        ),
        Mushroom(
            id = "mao14",
            name = "巨大傲娇猫爪菌",
            starter = "MAO",
            wood = "LV_SONG",
            light = "YU_RONG",
            humidifier = "TAO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "mao15",
            name = "迷你暗黑猫爪菌",
            starter = "MAO",
            wood = "LV_SONG",
            light = "YU_RONG",
            humidifier = "TAO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "mao16",
            name = "巨大暗黑猫爪菌",
            starter = "MAO",
            wood = "LV_SONG",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "mao17",
            name = "迷你撞色猫爪菌",
            starter = "MAO",
            wood = "LV_SONG",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "mao18",
            name = "巨大撞色猫爪菌",
            starter = "MAO",
            wood = "LV_SONG",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "pao0",
            name = "未能长大的泡泡菌",
            starter = "PAO",
            special = "BUG",
            save = false
        ),
        Mushroom(id = "pao1", name = "常见实习泡泡菌", starter = "PAO", wood = "BAI"),
        Mushroom(
            id = "pao2",
            name = "常见新手泡泡菌",
            starter = "PAO",
            wood = "BAI",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "pao3",
            name = "常见菜色泡泡菌",
            starter = "PAO",
            wood = "BAI",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "pao4",
            name = "常见看破一切泡泡菌",
            starter = "PAO",
            wood = "LOVE",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "pao5",
            name = "常见过劳泡泡菌",
            starter = "PAO",
            wood = "LOVE",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "pao6",
            name = "超小亚健康泡泡菌",
            starter = "PAO",
            wood = "LOVE",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "pao7",
            name = "超小实习泡泡菌",
            starter = "PAO",
            wood = "LOVE",
            light = "HUN",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "pao8",
            name = "超大亚健康泡泡菌",
            starter = "PAO",
            wood = "LOVE",
            light = "HUN",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "pao9",
            name = "超小过劳泡泡菌",
            starter = "PAO",
            wood = "LOVE",
            light = "HUN",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "pao10",
            name = "超小新手泡泡菌",
            starter = "PAO",
            wood = "QIAN_NIU",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "pao11",
            name = "常见亚健康泡泡菌",
            starter = "PAO",
            wood = "QIAN_NIU",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "pao12",
            name = "超大新手泡泡菌",
            starter = "PAO",
            wood = "QIAN_NIU",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "pao13",
            name = "超大过劳泡泡菌",
            starter = "PAO",
            wood = "JIAN_JING",
            light = "HUO",
            time = "DAY"
        ),
        Mushroom(
            id = "pao14",
            name = "超大实习泡泡菌",
            starter = "PAO",
            wood = "JIAN_JING",
            light = "HUO",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "pao15",
            name = "超小菜色泡泡菌",
            starter = "PAO",
            wood = "JIAN_JING",
            light = "HUO",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "pao16",
            name = "超大菜色泡泡菌",
            starter = "PAO",
            wood = "JIAN_JING",
            light = "HUO",
            humidifier = "NIAO",
            time = "NIGHT"
        ),
        Mushroom(
            id = "pao17",
            name = "超小看破一切泡泡菌",
            starter = "PAO",
            wood = "JIAN_JING",
            light = "HUO",
            humidifier = "NIAO",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "pao18",
            name = "超大看破一切泡泡菌",
            starter = "PAO",
            wood = "JIAN_JING",
            light = "HUO",
            humidifier = "NIAO",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cao0",
            name = "未能长大的草帽菌",
            starter = "CAO",
            special = "BUG",
            save = false
        ),
        Mushroom(id = "cao1", name = "普通老土草帽菌", starter = "CAO", wood = "BAI"),
        Mushroom(
            id = "cao2",
            name = "普通洁癖草帽菌",
            starter = "CAO",
            wood = "BAI",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cao3",
            name = "普通健美草帽菌",
            starter = "CAO",
            wood = "BAI",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cao4",
            name = "普通恶毒草帽菌",
            starter = "CAO",
            wood = "LOVE",
            light = "HUN",
            time = "DAY"
        ),
        Mushroom(
            id = "cao5",
            name = "普通蓝瘦草帽菌",
            starter = "CAO",
            wood = "LOVE",
            light = "HUN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cao6",
            name = "小斑比草帽菌",
            starter = "CAO",
            wood = "LOVE",
            light = "HUN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cao7",
            name = "小老土草帽菌",
            starter = "CAO",
            wood = "LOVE",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT"
        ),
        Mushroom(
            id = "cao8",
            name = "大斑比草帽菌",
            starter = "CAO",
            wood = "LOVE",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cao9",
            name = "小蓝瘦草帽菌",
            starter = "CAO",
            wood = "LOVE",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cao10",
            name = "小洁癖草帽菌",
            starter = "CAO",
            wood = "QIAN_NIU",
            light = "HUO",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "cao11",
            name = "普通斑比草帽菌",
            starter = "CAO",
            wood = "QIAN_NIU",
            light = "HUO",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cao12",
            name = "大洁癖草帽菌",
            starter = "CAO",
            wood = "QIAN_NIU",
            light = "HUO",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cao13",
            name = "大健美草帽菌",
            starter = "CAO",
            wood = "JIAN_JING",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "cao14",
            name = "小恶毒草帽菌",
            starter = "CAO",
            wood = "JIAN_JING",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cao15",
            name = "大恶毒草帽菌",
            starter = "CAO",
            wood = "JIAN_JING",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cao16",
            name = "大蓝瘦草帽菌",
            starter = "CAO",
            wood = "JIAN_JING",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "cao17",
            name = "大老土草帽菌",
            starter = "CAO",
            wood = "JIAN_JING",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cao18",
            name = "小健美草帽菌",
            starter = "CAO",
            wood = "JIAN_JING",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "shanhu0",
            name = "未能长大的珊瑚菌",
            starter = "SHAN_HU",
            special = "BUG",
            save = false
        ),
        Mushroom(id = "shanhu1", name = "中级贾诩珊瑚菌", starter = "SHAN_HU", wood = "FENG"),
        Mushroom(
            id = "shanhu2",
            name = "中级公孙珊珊瑚菌",
            starter = "SHAN_HU",
            wood = "FENG",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "shanhu3",
            name = "中级董奉珊瑚菌",
            starter = "SHAN_HU",
            wood = "FENG",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "shanhu4", name = "缩水公孙珊珊瑚菌", starter = "SHAN_HU", wood = "GE_TENG"),
        Mushroom(
            id = "shanhu5",
            name = "中级张郃珊瑚菌",
            starter = "SHAN_HU",
            wood = "GE_TENG",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "shanhu6",
            name = "膨胀公孙珊珊瑚菌",
            starter = "SHAN_HU",
            wood = "GE_TENG",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "shanhu7",
            name = "中级戏学珊瑚菌",
            starter = "SHAN_HU",
            wood = "JIAN_CI",
            light = "HUN",
            time = "DAY"
        ),
        Mushroom(
            id = "shanhu8",
            name = "中级陈登珊瑚菌",
            starter = "SHAN_HU",
            wood = "JIAN_CI",
            light = "HUN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "shanhu9",
            name = "缩水张郃珊瑚菌",
            starter = "SHAN_HU",
            wood = "JIAN_CI",
            light = "HUN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "shanhu10",
            name = "膨胀董奉珊瑚菌",
            starter = "SHAN_HU",
            wood = "LV_SONG",
            light = "HUO",
            time = "DAY"
        ),
        Mushroom(
            id = "shanhu11",
            name = "缩水戏学珊瑚菌",
            starter = "SHAN_HU",
            wood = "LV_SONG",
            light = "HUO",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "shanhu12",
            name = "膨胀戏学珊瑚菌",
            starter = "SHAN_HU",
            wood = "LV_SONG",
            light = "HUO",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "shanhu13",
            name = "膨胀陈登珊瑚菌",
            starter = "SHAN_HU",
            wood = "LV_SONG",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "shanhu14",
            name = "膨胀贾诩珊瑚菌",
            starter = "SHAN_HU",
            wood = "LV_SONG",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "shanhu15",
            name = "缩水董奉珊瑚菌",
            starter = "SHAN_HU",
            wood = "LV_SONG",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "shanhu16",
            name = "缩水贾诩珊瑚菌",
            starter = "SHAN_HU",
            wood = "JIAN_CI",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT"
        ),
        Mushroom(
            id = "shanhu17",
            name = "膨胀张郃珊瑚菌",
            starter = "SHAN_HU",
            wood = "JIAN_CI",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "shanhu18",
            name = "缩水陈登珊瑚菌",
            starter = "SHAN_HU",
            wood = "JIAN_CI",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "laba0",
            name = "未能长大的喇叭菌",
            starter = "LABA",
            special = "BUG",
            save = false
        ),
        Mushroom(
            id = "laba1",
            name = "寻常封心锁爱喇叭菌",
            starter = "LABA",
            wood = "FENG",
            light = "HUO"
        ),
        Mushroom(
            id = "laba2",
            name = "寻常黄金喇叭菌",
            starter = "LABA",
            wood = "FENG",
            light = "HUO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "laba3",
            name = "寻常陷阵营喇叭菌",
            starter = "LABA",
            wood = "FENG",
            light = "HUO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "laba4",
            name = "小巧黄金喇叭菌",
            starter = "LABA",
            wood = "GE_TENG",
            humidifier = "NIAO"
        ),
        Mushroom(
            id = "laba5",
            name = "寻常超火爆喇叭菌",
            starter = "LABA",
            wood = "GE_TENG",
            humidifier = "NIAO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "laba6",
            name = "特大黄金喇叭菌",
            starter = "LABA",
            wood = "GE_TENG",
            humidifier = "NIAO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "laba7",
            name = "小巧封心锁爱喇叭菌",
            starter = "LABA",
            wood = "JIAN_CI",
            light = "TONG",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "laba8",
            name = "特大超火爆喇叭菌",
            starter = "LABA",
            wood = "JIAN_CI",
            light = "TONG",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "laba9",
            name = "小巧青春痘喇叭菌",
            starter = "LABA",
            wood = "JIAN_CI",
            light = "TONG",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "laba10",
            name = "寻常夕阳喇叭菌",
            starter = "LABA",
            wood = "JIAN_CI",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "laba11",
            name = "寻常青春痘喇叭菌",
            starter = "LABA",
            wood = "JIAN_CI",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "laba12",
            name = "小巧超火爆喇叭菌",
            starter = "LABA",
            wood = "JIAN_CI",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "laba13",
            name = "特大青春痘喇叭菌",
            starter = "LABA",
            wood = "LV_SONG",
            light = "HUO",
            humidifier = "BLUE",
            time = "NIGHT"
        ),
        Mushroom(
            id = "laba14",
            name = "特大封心锁爱喇叭菌",
            starter = "LABA",
            wood = "LV_SONG",
            light = "HUO",
            humidifier = "BLUE",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "laba15",
            name = "小巧陷阵营喇叭菌",
            starter = "LABA",
            wood = "LV_SONG",
            light = "HUO",
            humidifier = "BLUE",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "laba16",
            name = "特大陷阵营喇叭菌",
            starter = "LABA",
            wood = "LV_SONG",
            light = "TONG",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "laba17",
            name = "小巧夕阳喇叭菌",
            starter = "LABA",
            wood = "LV_SONG",
            light = "TONG",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "laba18",
            name = "特大夕阳喇叭菌",
            starter = "LABA",
            wood = "LV_SONG",
            light = "TONG",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "di0",
            name = "未能长大的地星菌",
            starter = "DI",
            special = "BUG",
            save = false
        ),
        Mushroom(
            id = "di1",
            name = "路边朴素地星菌",
            starter = "DI",
            wood = "FENG",
            light = "HUO",
            humidifier = "ZHU"
        ),
        Mushroom(
            id = "di2",
            name = "路边森系地星菌",
            starter = "DI",
            wood = "FENG",
            light = "HUO",
            humidifier = "ZHU",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "di3",
            name = "路边荷塘夜色地星菌",
            starter = "DI",
            wood = "FENG",
            light = "HUO",
            humidifier = "ZHU",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "di4",
            name = "小只森系地星菌",
            starter = "DI",
            wood = "GE_TENG",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "di5",
            name = "路边粉毛地星菌",
            starter = "DI",
            wood = "GE_TENG",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "di6",
            name = "巨型森系地星菌",
            starter = "DI",
            wood = "GE_TENG",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "di7", name = "小只朴素地星菌", starter = "DI", wood = "YAN", light = "HUO"),
        Mushroom(
            id = "di8",
            name = "巨型粉毛地星菌",
            starter = "DI",
            wood = "YAN",
            light = "HUO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "di9",
            name = "小只轻骑兵地星菌",
            starter = "DI",
            wood = "YAN",
            light = "HUO",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "di10", name = "路边黄花菜地星菌", starter = "DI", wood = "YAN"),
        Mushroom(
            id = "di11",
            name = "路边轻骑兵地星菌",
            starter = "DI",
            wood = "YAN",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "di12",
            name = "小只粉毛地星菌",
            starter = "DI",
            wood = "YAN",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "di13",
            name = "巨型荷塘夜色地星菌",
            starter = "DI",
            wood = "SHU",
            light = "YU_RONG"
        ),
        Mushroom(
            id = "di14",
            name = "小只黄花菜地星菌",
            starter = "DI",
            wood = "SHU",
            light = "YU_RONG",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "di15",
            name = "巨型黄花菜地星菌",
            starter = "DI",
            wood = "SHU",
            light = "YU_RONG",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "di16", name = "巨型轻骑兵地星菌", starter = "DI", wood = "SHU"),
        Mushroom(
            id = "di17",
            name = "巨型朴素地星菌",
            starter = "DI",
            wood = "SHU",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "di18",
            name = "小只荷塘夜色地星菌",
            starter = "DI",
            wood = "SHU",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "deer0",
            name = "未能长大的鹿角菌",
            starter = "DEER",
            special = "BUG",
            save = false
        ),
        Mushroom(id = "deer1", name = "中庸素描鹿角菌", starter = "DEER", wood = "SONG"),
        Mushroom(
            id = "deer2",
            name = "中庸深渊鹿角菌",
            starter = "DEER",
            wood = "SONG",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "deer3",
            name = "中庸应援棒鹿角菌",
            starter = "DEER",
            wood = "SONG",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "deer4",
            name = "微型深渊鹿角菌",
            starter = "DEER",
            wood = "GE_TENG",
            light = "HUO",
            humidifier = "NIAO",
            time = "NIGHT"
        ),
        Mushroom(
            id = "deer5",
            name = "中庸二刺螈鹿角菌",
            starter = "DEER",
            wood = "GE_TENG",
            light = "HUO",
            humidifier = "NIAO",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "deer6",
            name = "巨体深渊鹿角菌",
            starter = "DEER",
            wood = "GE_TENG",
            light = "HUO",
            humidifier = "NIAO",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "deer7",
            name = "中庸蔫蔫鹿角菌",
            starter = "DEER",
            wood = "YAN",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "deer8",
            name = "中庸碧玉鹿角菌",
            starter = "DEER",
            wood = "YAN",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "deer9",
            name = "微型二刺螈鹿角菌",
            starter = "DEER",
            wood = "YAN",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "deer10",
            name = "巨体应援棒鹿角菌",
            starter = "DEER",
            wood = "MING",
            light = "YU_RONG"
        ),
        Mushroom(
            id = "deer11",
            name = "微型蔫蔫鹿角菌",
            starter = "DEER",
            wood = "MING",
            light = "YU_RONG",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "deer12",
            name = "巨体蔫蔫鹿角菌",
            starter = "DEER",
            wood = "MING",
            light = "YU_RONG",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "deer13",
            name = "微型素描鹿角菌",
            starter = "DEER",
            wood = "YAN",
            light = "HUN",
            humidifier = "BLUE",
            time = "NIGHT"
        ),
        Mushroom(
            id = "deer14",
            name = "巨体二刺螈鹿角菌",
            starter = "DEER",
            wood = "YAN",
            light = "HUN",
            humidifier = "BLUE",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "deer15",
            name = "微型碧玉鹿角菌",
            starter = "DEER",
            wood = "YAN",
            light = "HUN",
            humidifier = "BLUE",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "deer16", name = "巨体碧玉鹿角菌", starter = "DEER", wood = "MING"),
        Mushroom(
            id = "deer17",
            name = "巨体素描鹿角菌",
            starter = "DEER",
            wood = "MING",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "deer18",
            name = "微型应援棒鹿角菌",
            starter = "DEER",
            wood = "MING",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cai0",
            name = "未能长大的菜菜菌",
            starter = "CAI",
            special = "BUG",
            save = false
        ),
        Mushroom(
            id = "cai1",
            name = "通常西凉菜菜菌",
            starter = "CAI",
            wood = "SONG",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "cai2",
            name = "通常幽州菜菜菌",
            starter = "CAI",
            wood = "SONG",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cai3",
            name = "通常椰椰菜菜菌",
            starter = "CAI",
            wood = "SONG",
            light = "HUN",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cai4",
            name = "小型幽州菜菜菌",
            starter = "CAI",
            wood = "GE_TENG",
            light = "HUO",
            time = "DAY"
        ),
        Mushroom(
            id = "cai5",
            name = "通常水平太菜菜菌",
            starter = "CAI",
            wood = "GE_TENG",
            light = "HUO",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cai6",
            name = "大型幽州菜菜菌",
            starter = "CAI",
            wood = "GE_TENG",
            light = "HUO",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cai7",
            name = "小型西凉菜菜菌",
            starter = "CAI",
            wood = "YAN",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT"
        ),
        Mushroom(
            id = "cai8",
            name = "大型水平太菜菜菌",
            starter = "CAI",
            wood = "YAN",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cai9",
            name = "小型裂开菜菜菌",
            starter = "CAI",
            wood = "YAN",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cai10",
            name = "通常老菜菜菌",
            starter = "CAI",
            wood = "YAN",
            light = "HUN",
            time = "DAY"
        ),
        Mushroom(
            id = "cai11",
            name = "通常裂开菜菜菌",
            starter = "CAI",
            wood = "YAN",
            light = "HUN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cai12",
            name = "小型水平太菜菜菌",
            starter = "CAI",
            wood = "YAN",
            light = "HUN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cai13",
            name = "大型椰椰菜菜菌",
            starter = "CAI",
            wood = "SHU",
            light = "DRAGON",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "cai14",
            name = "小型老菜菜菌",
            starter = "CAI",
            wood = "SHU",
            light = "DRAGON",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cai15",
            name = "大型老菜菜菌",
            starter = "CAI",
            wood = "SHU",
            light = "DRAGON",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "cai16",
            name = "大型裂开菜菜菌",
            starter = "CAI",
            wood = "SHU",
            light = "YU_RONG",
            humidifier = "BLUE",
            time = "NIGHT"
        ),
        Mushroom(
            id = "cai17",
            name = "大型西凉菜菜菌",
            starter = "CAI",
            wood = "SHU",
            light = "YU_RONG",
            humidifier = "BLUE",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "cai18",
            name = "小型椰椰菜菜菌",
            starter = "CAI",
            wood = "SHU",
            light = "YU_RONG",
            humidifier = "BLUE",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ghost0",
            name = "未能长大的幽灵菌",
            starter = "GHOST",
            special = "BUG",
            save = false
        ),
        Mushroom(
            id = "ghost1",
            name = "中号暗黑幽灵菌",
            starter = "GHOST",
            wood = "SONG",
            light = "HUO"
        ),
        Mushroom(
            id = "ghost2",
            name = "中号挑染幽灵菌",
            starter = "GHOST",
            wood = "SONG",
            light = "HUO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ghost3",
            name = "中号蓝天白云幽灵菌",
            starter = "GHOST",
            wood = "SONG",
            light = "HUO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ghost4",
            name = "小号挑染幽灵菌",
            starter = "GHOST",
            wood = "GE_TENG",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "ghost5",
            name = "中号冰山幽灵菌",
            starter = "GHOST",
            wood = "GE_TENG",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ghost6",
            name = "大号挑染幽灵菌",
            starter = "GHOST",
            wood = "GE_TENG",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ghost7",
            name = "中号一口见阿祖幽灵菌",
            starter = "GHOST",
            wood = "YAN",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "ghost8",
            name = "中号幻象幽灵菌",
            starter = "GHOST",
            wood = "YAN",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ghost9",
            name = "小号冰山幽灵菌",
            starter = "GHOST",
            wood = "YAN",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ghost10",
            name = "小号暗黑幽灵菌",
            starter = "GHOST",
            wood = "YAN",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "ghost11",
            name = "大号冰山幽灵菌",
            starter = "GHOST",
            wood = "YAN",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ghost12",
            name = "小号幻象幽灵菌",
            starter = "GHOST",
            wood = "YAN",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ghost13",
            name = "大号幻象幽灵菌",
            starter = "GHOST",
            wood = "SHU",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "ghost14",
            name = "大号暗黑幽灵菌",
            starter = "GHOST",
            wood = "SHU",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ghost15",
            name = "小号蓝天白云幽灵菌",
            starter = "GHOST",
            wood = "SHU",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ghost16",
            name = "大号蓝天白云幽灵菌",
            starter = "GHOST",
            wood = "SHU",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "ghost17",
            name = "小号一口见阿祖幽灵菌",
            starter = "GHOST",
            wood = "SHU",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ghost18",
            name = "大号一口见阿祖幽灵菌",
            starter = "GHOST",
            wood = "SHU",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "niao0",
            name = "未能长大的鸟巢菌",
            starter = "NIAO",
            special = "BUG",
            save = false
        ),
        Mushroom(id = "niao1", name = "超小型陶瓷鸟巢菌", starter = "NIAO", wood = "LOVE"),
        Mushroom(
            id = "niao2",
            name = "标准厨余鸟巢菌",
            starter = "NIAO",
            wood = "LOVE",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "niao3",
            name = "超大型陶瓷鸟巢菌",
            starter = "NIAO",
            wood = "LOVE",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "niao4",
            name = "标准不可燃鸟巢菌",
            starter = "NIAO",
            wood = "CHANG_CHUN",
            light = "HUO",
            humidifier = "NIAO",
            time = "DAY"
        ),
        Mushroom(
            id = "niao5",
            name = "标准陶瓷鸟巢菌",
            starter = "NIAO",
            wood = "CHANG_CHUN",
            light = "HUO",
            humidifier = "NIAO",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "niao6",
            name = "标准可燃鸟巢菌",
            starter = "NIAO",
            wood = "CHANG_CHUN",
            light = "HUO",
            humidifier = "NIAO",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "niao7",
            name = "超小型不可燃鸟巢菌",
            starter = "NIAO",
            wood = "QING",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "niao8",
            name = "超大型厨余鸟巢菌",
            starter = "NIAO",
            wood = "QING",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "niao9",
            name = "超小型木桩鸟巢菌",
            starter = "NIAO",
            wood = "QING",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "niao10",
            name = "超大型可燃鸟巢菌",
            starter = "NIAO",
            wood = "MING",
            light = "DRAGON",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "niao11",
            name = "超小型五彩豆鸟巢菌",
            starter = "NIAO",
            wood = "MING",
            light = "DRAGON",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "niao12",
            name = "超大型五彩豆鸟巢菌",
            starter = "NIAO",
            wood = "MING",
            light = "DRAGON",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "niao13",
            name = "超大型木桩鸟巢菌",
            starter = "NIAO",
            wood = "MING",
            light = "TONG",
            humidifier = "ZHU",
            time = "NIGHT"
        ),
        Mushroom(
            id = "niao14",
            name = "超大型不可燃鸟巢菌",
            starter = "NIAO",
            wood = "MING",
            light = "TONG",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "niao15",
            name = "超小型可燃鸟巢菌",
            starter = "NIAO",
            wood = "MING",
            light = "TONG",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "niao16",
            name = "标准五彩豆鸟巢菌",
            starter = "NIAO",
            wood = "QING",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "niao17",
            name = "标准木桩鸟巢菌",
            starter = "NIAO",
            wood = "QING",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "niao18",
            name = "超小型厨余鸟巢菌",
            starter = "NIAO",
            wood = "QING",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ling0",
            name = "未能长大的美灵芝",
            starter = "LING",
            special = "BUG",
            save = false
        ),
        Mushroom(
            id = "ling1",
            name = "纤小红颜美灵芝",
            starter = "LING",
            wood = "LOVE",
            light = "HUO"
        ),
        Mushroom(
            id = "ling2",
            name = "寻常滞销美灵芝",
            starter = "LING",
            wood = "LOVE",
            light = "HUO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ling3",
            name = "硕大红颜美灵芝",
            starter = "LING",
            wood = "LOVE",
            light = "HUO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ling4",
            name = "寻常瘦身美灵芝",
            starter = "LING",
            wood = "CHANG_CHUN",
            light = "HUN",
            humidifier = "NIAO",
            time = "NIGHT"
        ),
        Mushroom(
            id = "ling5",
            name = "寻常红颜美灵芝",
            starter = "LING",
            wood = "CHANG_CHUN",
            light = "HUN",
            humidifier = "NIAO",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ling6",
            name = "寻常阴湿美灵芝",
            starter = "LING",
            wood = "CHANG_CHUN",
            light = "HUN",
            humidifier = "NIAO",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ling7",
            name = "纤小瘦身美灵芝",
            starter = "LING",
            wood = "QING",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT"
        ),
        Mushroom(
            id = "ling8",
            name = "硕大滞销美灵芝",
            starter = "LING",
            wood = "QING",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ling9",
            name = "纤小苍白美灵芝",
            starter = "LING",
            wood = "QING",
            light = "HUN",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ling10",
            name = "寻常潮人美灵芝",
            starter = "LING",
            wood = "QING",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "ling11",
            name = "寻常苍白美灵芝",
            starter = "LING",
            wood = "QING",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ling12",
            name = "纤小滞销美灵芝",
            starter = "LING",
            wood = "QING",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ling13",
            name = "硕大阴湿美灵芝",
            starter = "LING",
            wood = "GOLD",
            light = "YU_RONG",
            humidifier = "BLUE"
        ),
        Mushroom(
            id = "ling14",
            name = "纤小潮人美灵芝",
            starter = "LING",
            wood = "GOLD",
            light = "YU_RONG",
            humidifier = "BLUE",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ling15",
            name = "硕大潮人美灵芝",
            starter = "LING",
            wood = "GOLD",
            light = "YU_RONG",
            humidifier = "BLUE",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "ling16", name = "硕大苍白美灵芝", starter = "LING", wood = "GOLD"),
        Mushroom(
            id = "ling17",
            name = "硕大瘦身美灵芝",
            starter = "LING",
            wood = "GOLD",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ling18",
            name = "纤小阴湿美灵芝",
            starter = "LING",
            wood = "GOLD",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "zhu0",
            name = "未能长大的竹荪",
            starter = "ZHU",
            special = "BUG",
            save = false
        ),
        Mushroom(
            id = "zhu1",
            name = "清淡莲藕味竹荪",
            starter = "ZHU",
            wood = "LOVE",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "zhu2",
            name = "适中猪肉味竹荪",
            starter = "ZHU",
            wood = "LOVE",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "zhu3",
            name = "浓厚莲藕味竹荪",
            starter = "ZHU",
            wood = "LOVE",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "zhu4", name = "适中黄花菜味竹荪", starter = "ZHU", wood = "CHANG_CHUN"),
        Mushroom(
            id = "zhu5",
            name = "适中莲藕味竹荪",
            starter = "ZHU",
            wood = "CHANG_CHUN",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "zhu6",
            name = "适中番茄味竹荪",
            starter = "ZHU",
            wood = "CHANG_CHUN",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "zhu7",
            name = "适中蓝莓味竹荪",
            starter = "ZHU",
            wood = "QING",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "zhu8",
            name = "适中墨鱼丸竹荪",
            starter = "ZHU",
            wood = "QING",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "zhu9",
            name = "清淡猪肉味竹荪",
            starter = "ZHU",
            wood = "QING",
            light = "HUO",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "zhu10",
            name = "浓厚墨鱼丸竹荪",
            starter = "ZHU",
            wood = "GOLD",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY"
        ),
        Mushroom(
            id = "zhu11",
            name = "浓厚黄花菜味竹荪",
            starter = "ZHU",
            wood = "GOLD",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "zhu12",
            name = "清淡番茄味竹荪",
            starter = "ZHU",
            wood = "GOLD",
            light = "YU_RONG",
            humidifier = "ZHU",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "zhu13",
            name = "浓厚番茄味竹荪",
            starter = "ZHU",
            wood = "GOLD",
            light = "HUN",
            humidifier = "BLUE",
            time = "DAY"
        ),
        Mushroom(
            id = "zhu14",
            name = "清淡蓝莓味竹荪",
            starter = "ZHU",
            wood = "GOLD",
            light = "HUN",
            humidifier = "BLUE",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "zhu15",
            name = "浓厚蓝莓味竹荪",
            starter = "ZHU",
            wood = "GOLD",
            light = "HUN",
            humidifier = "BLUE",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "zhu16",
            name = "清淡黄花菜味竹荪",
            starter = "ZHU",
            wood = "QING",
            light = "HUN",
            humidifier = "LIAN",
            time = "NIGHT"
        ),
        Mushroom(
            id = "zhu17",
            name = "浓厚猪肉味竹荪",
            starter = "ZHU",
            wood = "QING",
            light = "HUN",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "zhu18",
            name = "清淡墨鱼丸竹荪",
            starter = "ZHU",
            wood = "QING",
            light = "HUN",
            humidifier = "LIAN",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ruby0",
            name = "未能长大的宝石菌",
            starter = "RUBY",
            special = "BUG",
            save = false
        ),
        Mushroom(
            id = "ruby1",
            name = "小小躺板板宝石菌",
            starter = "RUBY",
            wood = "LOVE",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "ruby2",
            name = "常规坏种宝石菌",
            starter = "RUBY",
            wood = "LOVE",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ruby3",
            name = "庞大躺板板宝石菌",
            starter = "RUBY",
            wood = "LOVE",
            light = "YU_RONG",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "ruby4", name = "常规番茄炒蛋宝石菌", starter = "RUBY", wood = "QIAN_NIU"),
        Mushroom(
            id = "ruby5",
            name = "常规躺板板宝石菌",
            starter = "RUBY",
            wood = "QIAN_NIU",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ruby6",
            name = "常规白切黑宝石菌",
            starter = "RUBY",
            wood = "QIAN_NIU",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "ruby7", name = "常规酥酪宝石菌", starter = "RUBY", wood = "JIAN_JING"),
        Mushroom(
            id = "ruby8",
            name = "常规夜光宝石菌",
            starter = "RUBY",
            wood = "JIAN_JING",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ruby9",
            name = "小小坏种宝石菌",
            starter = "RUBY",
            wood = "JIAN_JING",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ruby10",
            name = "小小番茄炒蛋宝石菌",
            starter = "RUBY",
            wood = "JIAN_JING",
            light = "DRAGON",
            humidifier = "NIAO"
        ),
        Mushroom(
            id = "ruby11",
            name = "庞大坏种宝石菌",
            starter = "RUBY",
            wood = "JIAN_JING",
            light = "DRAGON",
            humidifier = "NIAO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ruby12",
            name = "小小夜光宝石菌",
            starter = "RUBY",
            wood = "JIAN_JING",
            light = "DRAGON",
            humidifier = "NIAO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ruby13",
            name = "庞大夜光宝石菌",
            starter = "RUBY",
            wood = "GOLD",
            light = "TONG",
            humidifier = "ZHU",
            time = "NIGHT"
        ),
        Mushroom(
            id = "ruby14",
            name = "庞大番茄炒蛋宝石菌",
            starter = "RUBY",
            wood = "GOLD",
            light = "TONG",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ruby15",
            name = "小小白切黑宝石菌",
            starter = "RUBY",
            wood = "GOLD",
            light = "TONG",
            humidifier = "ZHU",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "ruby16",
            name = "庞大白切黑宝石菌",
            starter = "RUBY",
            wood = "GOLD",
            light = "HUO",
            humidifier = "TAO",
            time = "NIGHT"
        ),
        Mushroom(
            id = "ruby17",
            name = "小小酥酪宝石菌",
            starter = "RUBY",
            wood = "GOLD",
            light = "HUO",
            humidifier = "TAO",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "ruby18",
            name = "庞大酥酪宝石菌",
            starter = "RUBY",
            wood = "GOLD",
            light = "HUO",
            humidifier = "TAO",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "hou0",
            name = "未能长大的猴头菌",
            starter = "HOU",
            special = "BUG",
            save = false
        ),
        Mushroom(
            id = "hou1",
            name = "平凡社牛猴头菌",
            starter = "HOU",
            wood = "CHANG_CHUN",
            light = "HUO"
        ),
        Mushroom(
            id = "hou2",
            name = "平凡笨蛋猴头菌",
            starter = "HOU",
            wood = "CHANG_CHUN",
            light = "HUO",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "hou3",
            name = "平凡宅宅猴头菌",
            starter = "HOU",
            wood = "CHANG_CHUN",
            light = "HUO",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "hou4",
            name = "微小笨蛋猴头菌",
            starter = "HOU",
            wood = "GE_TENG",
            light = "TONG",
            time = "NIGHT"
        ),
        Mushroom(
            id = "hou5",
            name = "平凡高智商猴头菌",
            starter = "HOU",
            wood = "GE_TENG",
            light = "TONG",
            time = "NIGHT",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "hou6",
            name = "超级笨蛋猴头菌",
            starter = "HOU",
            wood = "GE_TENG",
            light = "TONG",
            time = "NIGHT",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "hou7",
            name = "微小社牛猴头菌",
            starter = "HOU",
            wood = "QING",
            light = "HUO",
            humidifier = "LIAN"
        ),
        Mushroom(
            id = "hou8",
            name = "超级高智商猴头菌",
            starter = "HOU",
            wood = "QING",
            light = "HUO",
            humidifier = "LIAN",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "hou9",
            name = "微小逃课猴头菌",
            starter = "HOU",
            wood = "QING",
            light = "HUO",
            humidifier = "LIAN",
            special = "MUCH",
            save = true
        ),
        Mushroom(id = "hou10", name = "平凡隐鸢阁冠名猴头菌", starter = "HOU", wood = "QING"),
        Mushroom(
            id = "hou11",
            name = "平凡逃课猴头菌",
            starter = "HOU",
            wood = "QING",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "hou12",
            name = "微小高智商猴头菌",
            starter = "HOU",
            wood = "QING",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "hou13",
            name = "超级逃课猴头菌",
            starter = "HOU",
            wood = "MING",
            light = "HUO",
            humidifier = "NIAO",
            time = "DAY"
        ),
        Mushroom(
            id = "hou14",
            name = "超级社牛猴头菌",
            starter = "HOU",
            wood = "MING",
            light = "HUO",
            humidifier = "NIAO",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "hou15",
            name = "微小宅宅猴头菌",
            starter = "HOU",
            wood = "MING",
            light = "HUO",
            humidifier = "NIAO",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
        Mushroom(
            id = "hou16",
            name = "超级宅宅猴头菌",
            starter = "HOU",
            wood = "MING",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY"
        ),
        Mushroom(
            id = "hou17",
            name = "微小隐鸢阁冠名猴头菌",
            starter = "HOU",
            wood = "MING",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY",
            special = "LESS",
            save = true
        ),
        Mushroom(
            id = "hou18",
            name = "超级隐鸢阁冠名猴头菌",
            starter = "HOU",
            wood = "MING",
            light = "HUN",
            humidifier = "LIAN",
            time = "DAY",
            special = "MUCH",
            save = true
        ),
    )

    // 提供一个只包含名字的列表，给 OCR 匹配用
    // lazy 意味着只有第一次用到时才会计算，节省性能
    val allNames: List<String> by lazy {
        list.map { it.name }
    }
}