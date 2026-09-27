package cn.ppps.forwarder.utils

import android.graphics.Color
import android.text.TextUtils
import java.util.*

/**
 * <pre>
 * desc   : Random Utils
 * author : xuexiang
</pre> *
 *
 * Shuffling algorithm
 *  * [.shuffle] Shuffling algorithm, Randomly permutes the specified array using a default source of
 * randomness
 *  * [.shuffle] Shuffling algorithm, Randomly permutes the specified array
 *  * [.shuffle] Shuffling algorithm, Randomly permutes the specified int array using a default source of
 * randomness
 *  * [.shuffle] Shuffling algorithm, Randomly permutes the specified int array
 *
 *
 * get random int
 *  * [.getRandom] get random int between 0 and max
 *  * [.getRandom] get random int between min and max
 *
 *
 * get random numbers or letters
 *  * [.getRandomCapitalLetters] get a fixed-length random string, its a mixture of uppercase letters
 *  * [.getRandomLetters] get a fixed-length random string, its a mixture of uppercase and lowercase letters
 *
 *  * [.getRandomLowerCaseLetters] get a fixed-length random string, its a mixture of lowercase letters
 *  * [.getRandomNumbers] get a fixed-length random string, its a mixture of numbers
 *  * [.getRandomNumbersAndLetters] get a fixed-length random string, its a mixture of uppercase, lowercase
 * letters and numbers
 *  * [.getRandom] get a fixed-length random string, its a mixture of chars in source
 *  * [.getRandom] get a fixed-length random string, its a mixture of chars in sourceChar
 *
 *
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class RandomUtils private constructor() {
    companion object {
        private const val NUMBERS_AND_LETTERS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val NUMBERS = "0123456789"
        private const val LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"

        /**
         *
         * @see RandomUtils.getRandom
         */
        @JvmStatic
        fun getRandomNumbersAndLetters(length: Int): String? {
            return getRandom(NUMBERS_AND_LETTERS, length)
        }

        /**
         *
         * @see RandomUtils.getRandom
         */
        fun getRandomNumbers(length: Int): String? {
            return getRandom(NUMBERS, length)
        }

        /**
         *
         * @see RandomUtils.getRandom
         */
        fun getRandomLetters(length: Int): String? {
            return getRandom(LETTERS, length)
        }

        /**
         *
         * @see RandomUtils.getRandom
         */
        fun getRandomCapitalLetters(length: Int): String? {
            return getRandom(CAPITAL_LETTERS, length)
        }

        /**
         *
         * @see RandomUtils.getRandom
         */
        fun getRandomLowerCaseLetters(length: Int): String? {
            return getRandom(LOWER_CASE_LETTERS, length)
        }

        /**
         *
         * @return
         *  * if source is null or empty, return null
         *  * else see [RandomUtils.getRandom]
         *
         */
        fun getRandom(source: String, length: Int): String? {
            return if (TextUtils.isEmpty(source)) null else getRandom(source.toCharArray(), length)
        }

        /**
         *
         * @return
         *  * if sourceChar is null or empty, return null
         *  * if length less than 0, return null
         *
         */
        fun getRandom(sourceChar: CharArray?, length: Int): String? {
            if (sourceChar == null || sourceChar.isEmpty() || length < 0) {
                return null
            }
            val str = StringBuilder(length)
            val random = Random()
            for (i in 0 until length) {
                str.append(sourceChar[random.nextInt(sourceChar.size)])
            }
            return str.toString()
        }

        /**
         * get random int between 0 and max
         *
         * @return
         *  * if max <= 0, return 0
         *  * else return random int between 0 and max
         *
         */
        fun getRandom(max: Int): Int {
            return getRandom(0, max)
        }

        /**
         * get random int between min and max
         *
         * @return
         *  * if min > max, return 0
         *  * if min == max, return min
         *  * else return random int between min and max
         *
         */
        fun getRandom(min: Int, max: Int): Int {
            if (min > max) {
                return 0
            }
            return if (min == max) {
                min
            } else min + Random().nextInt(max - min)
        }

        /**
         *
         * @return
         */
        val randomColor: Int
            get() {
                val random = Random()
                val r = random.nextInt(256)
                val g = random.nextInt(256)
                val b = random.nextInt(256)
                return Color.rgb(r, g, b)
            }

        /**
         *
         * @param objArray
         * @return
         */
        fun shuffle(objArray: Array<Any?>?): Boolean {
            return if (objArray == null) {
                false
            } else shuffle(
                objArray,
                getRandom(objArray.size)
            )
        }

        /**
         *
         * @param objArray
         * @param shuffleCount
         * @return
         */
        private fun shuffle(objArray: Array<Any?>?, shuffleCount: Int): Boolean {
            var length = 0
            if (objArray == null || shuffleCount < 0 || objArray.size.also {
                    length = it
                } < shuffleCount) {
                return false
            }
            for (i in 1..shuffleCount) {
                val random = getRandom(length - i)
                val temp = objArray[length - i]
                objArray[length - i] = objArray[random]
                objArray[random] = temp
            }
            return true
        }

        /**
         *
         * @param intArray
         * @return
         */
        fun shuffle(intArray: IntArray?): IntArray? {
            return if (intArray == null) {
                null
            } else shuffle(
                intArray,
                getRandom(intArray.size)
            )
        }

        /**
         *
         * @param intArray
         * @param shuffleCount
         * @return
         */
        fun shuffle(intArray: IntArray?, shuffleCount: Int): IntArray? {
            var length = 0
            if (intArray == null || shuffleCount < 0 || intArray.size.also {
                    length = it
                } < shuffleCount) {
                return null
            }
            val out = IntArray(shuffleCount)
            for (i in 1..shuffleCount) {
                val random = getRandom(length - i)
                out[i - 1] = intArray[random]
                val temp = intArray[length - i]
                intArray[length - i] = intArray[random]
                intArray[random] = temp
            }
            return out
        }
    }

    /**
     * Don't let anyone instantiate this class.
     */
    init {
        throw Error("Do not need instantiate!")
    }
}