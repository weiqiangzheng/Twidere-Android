/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.statuses

import android.os.Bundle
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_EXTRAS
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.fragment.CursorStatusesFragment
import org.mariotaku.twidere.model.ParameterizedExpression
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.refresh.RefreshTaskParam
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetPublicTimelineTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.ErrorInfoStore
import java.util.*

/**
 * Created by mariotaku on 14/12/2.
 */
class PublicTimelineFragment : CursorStatusesFragment() {

    override val errorInfoKey = ErrorInfoStore.KEY_PUBLIC_TIMELINE

    override val contentUri = Statuses.Public.CONTENT_URI

    override val notificationType = 0

    override val isFilterEnabled = true

    override val readPositionTag = ReadPositionTag.PUBLIC_TIMELINE

    override val timelineSyncTag: String?
        get() = getTimelineSyncTag(accountKeys)

    override val filterScopes: Int
        get() = FilterScope.PUBLIC_TIMELINE

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.title_public_timeline)
    }

    override fun updateRefreshState() {
        val twitter = twitterWrapper
        refreshing = twitter.isStatusTimelineRefreshing(contentUri)
    }

    override fun getStatuses(param: RefreshTaskParam): Boolean {
        val task = GetPublicTimelineTask(context)
        task.params = param
        TaskStarter.execute(task)
        return true
    }

    override fun processWhere(where: Expression, whereArgs: Array<String>): ParameterizedExpression {
        val arguments = arguments
        if (arguments != null) {
            val extras = arguments.getParcelable<HomeTabExtras?>(EXTRA_EXTRAS)
            if (extras != null) {
                val expressions = ArrayList<Expression>()
                val expressionArgs = ArrayList<String>()
                Collections.addAll(expressionArgs, *whereArgs)
                expressions.add(where)
                DataStoreUtils.processTabExtras(expressions, expressionArgs, extras)
                val expression = Expression.and(*expressions.toTypedArray())
                return ParameterizedExpression(expression, expressionArgs.toTypedArray())
            }
        }
        return super.processWhere(where, whereArgs)
    }

    companion object {

        fun getTimelineSyncTag(accountKeys: Array<UserKey>): String {
            return "${ReadPositionTag.PUBLIC_TIMELINE}_${accountKeys.sorted().joinToString(",")}"
        }

    }
}

