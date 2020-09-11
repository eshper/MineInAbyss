package com.derongan.minecraft.mineinabyss.commands

import com.derongan.minecraft.mineinabyss.AbyssContext
import com.derongan.minecraft.mineinabyss.gui.GondolaGUI
import com.derongan.minecraft.mineinabyss.gui.StatsGUI
import com.derongan.minecraft.mineinabyss.mineInAbyss
import com.derongan.minecraft.mineinabyss.playerData
import com.mineinabyss.idofront.commands.arguments.intArg
import com.mineinabyss.idofront.commands.arguments.stringArg
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.execution.stopCommand
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.items.editItemMeta
import com.mineinabyss.idofront.messaging.color
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

@ExperimentalCommandDSL
object GUICommandExecutor : IdofrontCommandExecutor() {
    private val leaveConfirm = ArrayList<UUID>()

    override val commands = commands(mineInAbyss) {
        "stats" {
            "subcommand" {

            }
            playerAction {
                StatsGUI(player).show(player)
            }
        }
        "start" command@{
            playerAction {
                if (player.playerData.isIngame)
                    //TODO allow access to stopCommand directly from here
                    this@command.stopCommand("You are already ingame!\nYou can leave using /stopdescent")
                GondolaGUI(player).show(player)
            }
        }
        "stopdescent" {
            playerAction {
                if (!player.playerData.isIngame) {
                    sender.error("You are not currently ingame!\nStart by using /start")
                } else if (!leaveConfirm.contains(player.uniqueId)) {
                    leaveConfirm.add(player.uniqueId)
                    sender.info("""
                                &cYou are about to leave the game!!!
                                &lYour progress will be lost&r&c, but any xp and money you earned will stay with you.
                                Type /stopdescent again to leave""".trimIndent().color())
                } else {
                    leaveConfirm.remove(player.uniqueId)
                    player.health = 0.0
                }
            }
        }
        "creategondolaspawn" {
            val displayName by stringArg { default = "" }
            val cost by intArg { default = 0 }

            playerAction {
                val spawnLocConfig = AbyssContext.configManager.startLocationCM
                val spawns = spawnLocConfig.getMapList(GondolaGUI.SPAWN_KEY)
                var displayItem = player.inventory.itemInMainHand.clone()

                if (displayItem.type == Material.AIR)
                    displayItem = ItemStack(Material.GRASS_BLOCK)

                displayItem.editItemMeta {
                    if (displayName != "")
                        setDisplayName(displayName)
                    addItemFlags(ItemFlag.HIDE_ATTRIBUTES) //TODO probably better to add these tags to the serialized items
                    addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                }

                val map = mutableMapOf<String, Any>()
                map["location"] = player.location
                map["display-item"] = displayItem
                if (cost != 0)
                    map["cost"] = cost

                spawns.add(map)
                spawnLocConfig.set(GondolaGUI.SPAWN_KEY, spawns)
                spawnLocConfig.saveConfig()

                sender.success("Created spawn")
            }
        }
    }
}
