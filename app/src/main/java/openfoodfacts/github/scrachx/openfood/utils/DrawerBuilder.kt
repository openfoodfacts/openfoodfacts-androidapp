package openfoodfacts.github.scrachx.openfood.utils

import android.app.Activity
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.*

inline fun buildDrawer(activity: Activity, builderAction: DrawerBuilder.() -> Unit = {}): Drawer {
    return DrawerBuilder(activity).apply(builderAction).build()
}

inline fun primaryItem(builderAction: PrimaryDrawerItem.() -> Unit = {}): PrimaryDrawerItem {
    return PrimaryDrawerItem().apply(builderAction)
}

inline fun sectionItem(builderAction: SectionDrawerItem.() -> Unit = {}): SectionDrawerItem {
    return SectionDrawerItem().apply(builderAction)
}

fun dividerItem(builderAction: DividerDrawerItem.() -> Unit = {}): DividerDrawerItem {
    return DividerDrawerItem().apply(builderAction)
}

inline fun profileSettingItem(builderAction: ProfileSettingDrawerItem.() -> Unit = {}): ProfileSettingDrawerItem {
    return ProfileSettingDrawerItem().apply(builderAction)
}

inline fun profileItem(builderAction: ProfileDrawerItem.() -> Unit = {}): ProfileDrawerItem {
    return ProfileDrawerItem().apply(builderAction)
}

inline fun buildAccountHeader(builder: AccountHeaderBuilder.() -> Unit = {}): AccountHeader {
    return AccountHeaderBuilder().apply(builder).build()
}