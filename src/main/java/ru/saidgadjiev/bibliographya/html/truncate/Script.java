package ru.saidgadjiev.bibliographya.html.truncate;

/**
 * Created by said on 23/03/2019.
 */
public class Script {

    private Script() {}

    public static final String SCRIPT = "/**\n" +
            " * Truncate HTML string and keep tag safe.\n" +
            " *\n" +
            " * @method truncate\n" +
            " * @param {String} string string needs to be truncated\n" +
            " * @param {Number} maxLength length of truncated string\n" +
            " * @param {Object} options (optional)\n" +
            " * @param {Boolean} [options.keepImageTag] flag to specify if keep image tag, false by default\n" +
            " * @param {Boolean} [options.truncateLastWord] truncates last word, true by default\n" +
            " * @param {Number} [options.slop] tolerance when options.truncateLastWord is false before we give up and just truncate at the maxLength position, 10 by default (but not greater than maxLength)\n" +
            " * @param {Boolean|String} [options.ellipsis] omission symbol for truncated string, '...' by default\n" +
            " * @return {String} truncated string\n" +
            " */\n" +
            "function truncate(string, maxLength, options) {\n" +
            "    var EMPTY_OBJECT = {},\n" +
            "        EMPTY_STRING = '',\n" +
            "        DEFAULT_TRUNCATE_SYMBOL = '...',\n" +
            "        DEFAULT_SLOP = 10 > maxLength ? maxLength : 10,\n" +
            "        EXCLUDE_TAGS = ['img', 'br'],   // non-closed tags\n" +
            "        items = [],                     // stack for saving tags\n" +
            "        total = 0,                      // record how many characters we traced so far\n" +
            "        content = EMPTY_STRING,         // truncated text storage\n" +
            "        KEY_VALUE_REGEX = '([\\\\w|-]+\\\\s*(=\\\\s*\"[^\"]*\")?\\\\s*)*',\n" +
            "        IS_CLOSE_REGEX = '\\\\s*\\\\/?\\\\s*',\n" +
            "        CLOSE_REGEX = '\\\\s*\\\\/\\\\s*',\n" +
            "        SELF_CLOSE_REGEX = new RegExp('<\\\\/?\\\\w+\\\\s*' + KEY_VALUE_REGEX + CLOSE_REGEX + '>'),\n" +
            "        HTML_TAG_REGEX = new RegExp('<\\\\/?\\\\w+\\\\s*' + KEY_VALUE_REGEX + IS_CLOSE_REGEX + '>'),\n" +
            "        URL_REGEX = /(((ftp|https?):\\/\\/)[\\-\\w@:%_\\+.~#?,&\\/\\/=]+)|((mailto:)?[_.\\w\\-]+@([\\w][\\w\\-]+\\.)+[a-zA-Z]{2,3})/g, // Simple regexp\n" +
            "        IMAGE_TAG_REGEX = new RegExp('<img\\\\s*' + KEY_VALUE_REGEX + IS_CLOSE_REGEX + '>'),\n" +
            "        WORD_BREAK_REGEX = new RegExp('\\\\W+', 'g'),\n" +
            "        matches = true,\n" +
            "        result,\n" +
            "        index,\n" +
            "        tail,\n" +
            "        tag,\n" +
            "        selfClose;\n" +
            "\n" +
            "    /**\n" +
            "     * Remove image tag\n" +
            "     *\n" +
            "     * @private\n" +
            "     * @method _removeImageTag\n" +
            "     * @param {String} string not-yet-processed string\n" +
            "     * @return {String} string without image tags\n" +
            "     */\n" +
            "    function _removeImageTag(string) {\n" +
            "        var match = IMAGE_TAG_REGEX.exec(string),\n" +
            "            index,\n" +
            "            len;\n" +
            "\n" +
            "        if (!match) {\n" +
            "            return string;\n" +
            "        }\n" +
            "\n" +
            "        index = match.index;\n" +
            "        len = match[0].length;\n" +
            "\n" +
            "        return string.substring(0, index) + string.substring(index + len);\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * Dump all close tags and append to truncated content while reaching upperbound\n" +
            "     *\n" +
            "     * @private\n" +
            "     * @method _dumpCloseTag\n" +
            "     * @param {String[]} tags a list of tags which should be closed\n" +
            "     * @return {String} well-formatted html\n" +
            "     */\n" +
            "    function _dumpCloseTag(tags) {\n" +
            "        var html = '';\n" +
            "\n" +
            "        tags.reverse().forEach(function (tag, index) {\n" +
            "            // dump non-excluded tags only\n" +
            "            if (-1 === EXCLUDE_TAGS.indexOf(tag)) {\n" +
            "                html += '</' + tag + '>';\n" +
            "            }\n" +
            "        });\n" +
            "\n" +
            "        return html;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * Process tag string to get pure tag name\n" +
            "     *\n" +
            "     * @private\n" +
            "     * @method _getTag\n" +
            "     * @param {String} string original html\n" +
            "     * @return {String} tag name\n" +
            "     */\n" +
            "    function _getTag(string) {\n" +
            "        var tail = string.indexOf(' ');\n" +
            "\n" +
            "        // TODO:\n" +
            "        // we have to figure out how to handle non-well-formatted HTML case\n" +
            "        if (-1 === tail) {\n" +
            "            tail = string.indexOf('>');\n" +
            "            if (-1 === tail) {\n" +
            "                throw new Error('HTML tag is not well-formed : ' + string);\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        return string.substring(1, tail);\n" +
            "    }\n" +
            "\n" +
            "\n" +
            "    /**\n" +
            "     * Get the end position for String#substring()\n" +
            "     *\n" +
            "     * If options.truncateLastWord is FALSE, we try to the end position up to\n" +
            "     * options.slop characters to avoid breaking in the middle of a word.\n" +
            "     *\n" +
            "     * @private\n" +
            "     * @method _getEndPosition\n" +
            "     * @param {String} string original html\n" +
            "     * @param {Number} tailPos (optional) provided to avoid extending the slop into trailing HTML tag\n" +
            "     * @return {Number} maxLength\n" +
            "     */\n" +
            "    function _getEndPosition (string, tailPos) {\n" +
            "        var defaultPos = maxLength - total,\n" +
            "            position = defaultPos,\n" +
            "            isShort = defaultPos < options.slop,\n" +
            "            slopPos = isShort ? defaultPos : options.slop - 1,\n" +
            "            substr,\n" +
            "            startSlice = isShort ? 0 : defaultPos - options.slop,\n" +
            "            endSlice = tailPos || (defaultPos + options.slop),\n" +
            "            result;\n" +
            "\n" +
            "        if (!options.truncateLastWord) {\n" +
            "\n" +
            "            substr = string.slice(startSlice, endSlice);\n" +
            "\n" +
            "            if (tailPos && substr.length <= tailPos) {\n" +
            "                position = substr.length;\n" +
            "            }\n" +
            "            else {\n" +
            "                while ((result = WORD_BREAK_REGEX.exec(substr)) !== null) {\n" +
            "                    // a natural break position before the hard break position\n" +
            "                    if (result.index < slopPos) {\n" +
            "                        position = defaultPos - (slopPos - result.index);\n" +
            "                        // keep seeking closer to the hard break position\n" +
            "                        // unless a natural break is at position 0\n" +
            "                        if (result.index === 0 && defaultPos <= 1) break;\n" +
            "                    }\n" +
            "                    // a natural break position exactly at the hard break position\n" +
            "                    else if (result.index === slopPos) {\n" +
            "                        position = defaultPos;\n" +
            "                        break; // seek no more\n" +
            "                    }\n" +
            "                    // a natural break position after the hard break position\n" +
            "                    else {\n" +
            "                        position = defaultPos + (result.index - slopPos);\n" +
            "                        break;  // seek no more\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "            if (string.charAt(position - 1).match(/\\s$/)) position--;\n" +
            "        }\n" +
            "        return position;\n" +
            "    }\n" +
            "\n" +
            "    options = options || EMPTY_OBJECT;\n" +
            "    options.ellipsis = (undefined !== options.ellipsis) ? options.ellipsis : DEFAULT_TRUNCATE_SYMBOL;\n" +
            "    options.truncateLastWord = (undefined !== options.truncateLastWord) ? options.truncateLastWord : true;\n" +
            "    options.slop = (undefined !== options.slop) ? options.slop : DEFAULT_SLOP;\n" +
            "\n" +
            "    while (matches) {\n" +
            "        matches = HTML_TAG_REGEX.exec(string);\n" +
            "\n" +
            "        if (!matches) {\n" +
            "            if (total >= maxLength) { break; }\n" +
            "\n" +
            "            matches = URL_REGEX.exec(string);\n" +
            "            if (!matches || matches.index >= maxLength) {\n" +
            "                content += string.substring(0, _getEndPosition(string));\n" +
            "                break;\n" +
            "            }\n" +
            "\n" +
            "            while (matches) {\n" +
            "                result = matches[0];\n" +
            "                index = matches.index;\n" +
            "                content += string.substring(0, (index + result.length) - total);\n" +
            "                string = string.substring(index + result.length);\n" +
            "                matches = URL_REGEX.exec(string);\n" +
            "            }\n" +
            "            break;\n" +
            "        }\n" +
            "\n" +
            "        result = matches[0];\n" +
            "        index = matches.index;\n" +
            "\n" +
            "        if (total + index > maxLength) {\n" +
            "            // exceed given `maxLength`, dump everything to clear stack\n" +
            "            content += string.substring(0, _getEndPosition(string, index));\n" +
            "            break;\n" +
            "        } else {\n" +
            "            total += index;\n" +
            "            content += string.substring(0, index);\n" +
            "        }\n" +
            "\n" +
            "        if ('/' === result[1]) {\n" +
            "            // move out open tag\n" +
            "            items.pop();\n" +
            "            selfClose=null;\n" +
            "        } else {\n" +
            "            selfClose = SELF_CLOSE_REGEX.exec(result);\n" +
            "            if (!selfClose) {\n" +
            "                tag = _getTag(result);\n" +
            "\n" +
            "                items.push(tag);\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        if (selfClose) {\n" +
            "            content += selfClose[0];\n" +
            "        } else {\n" +
            "            content += result;\n" +
            "        }\n" +
            "        string = string.substring(index + result.length);\n" +
            "    }\n" +
            "\n" +
            "    if (string.length > maxLength - total && options.ellipsis) {\n" +
            "        content += options.ellipsis;\n" +
            "    }\n" +
            "    content += _dumpCloseTag(items);\n" +
            "\n" +
            "    if (!options.keepImageTag) {\n" +
            "        content = _removeImageTag(content);\n" +
            "    }\n" +
            "\n" +
            "    return content;\n" +
            "}\n" +
            "\n";
}
