# Object of the game

Return the eight pearls located the dungeons throughout the land
to the Pearl Temple.

# Keys

Use up and down keys to scroll help, any other key to continue playing.

Use the following keys to move, attack, go up/down stairs, or open adjacent doors:

| y | k | u |   | 7 | 8 | 9 |
| h |   | l |   | 4 |   | 6 |
| b | j | n |   | 1 | 2 | 3 |

You can also use the arrow keys, though diagonal movement is not possible.
Holding down shift while pressing the direction keys will run.
The keys . or number pad 5 indicate to stay still.

| Key  | Action                     |  Key  | Action                      |
| ---  | -------------------------  |  ---  | --------------------------  |
|   .  | Stay still                 |       |                             |
|   /  | See monster information    |    ?  | See help                    |
|   a  | Autoplay options           |       |                             |
|   b  | Walk southwest             |    B  | Run southwest               |
|   c  | Character information      |    C  | Close door                  |
|   e  | Show your equipment        |       |                             |
|   f  | Fire an arrow              |       |                             |
|   g  | Get/optimize equipment     |    G  | Show objects on the ground  |
|   h  | Walk west                  |    H  | Run west                    |
|   i  | Show your inventory        |       |                             |
|   j  | Walk south                 |    J  | Run south                   |
|   k  | Walk north                 |    K  | Run north                   |
|   l  | Walk east                  |    L  | Run east                    |
|   m  | Cast magic                 |    M  | Show world map              |
|   n  | Walk southeast             |    N  | Run southeast               |
|   t  | Target a monster           |    T  | Target a spot on the ground |
|   u  | Walk northeast             |    U  | Run northeast               |
|   x  | Examine nearby things      |       |                             |
|   y  | Walk northwest             |    Y  | Run northwest               |

## Debugging commands

| Key  | Action                    |
| ---  | ------------------------- |
|   (  | Show player AI info       |
|   )  | Show pet AI info          |

# Game Mechanics

## The World

Each new game has a different, randomly generated world, though within
a particular game, the world is permanent.  In particular, items left
on the ground will remain in place if you leave and come back later.
However monsters will regenerate in areas that you have not been to
for a while.

The game is saved after resting at an inn.  If you die, then you
can reopen the game where you last saved.

## Characters

There are four different characters you can choose to play.  Each type has a
somewhat different flavor, having different spells and strengths.

Archer: This character has average stats, but does more ranged damage (using
bows) than all other characters, so they will tend to favor long-range attacks.

Mage: This character is rather weak physically, but has an arsenal of powerful
magic to attack.

Warrior: This character is physically strong, favoring hand-to-hand combat.
Occasionally their attacks may stun an enemy, which lowers their defenses while
they recover.

Rogue: This character, though not as powerful as a warrior, is still quick and
agile. Their attacks may occasionally poison an enemy, sapping their strength
over a period of time.

Half Dragon: This character is physically stronger and more dexterous than any
human, also has the ability to do breath attacks.

## Pets

You will have the opportunity to gain a pet during the game.

The pets are as follows:

Fox: Similar to a mage, this animal is weaker physically but has powerful
magical abilities.

Panther: This animal is strong and fast; it does modest physical damage, but
emphasizes speed and dexterity.

Bear: While lacking in magical ability, this animal can both hit the hardest
and sustain the most damage.

Note that pets cannot carry items, wear armor, or wield weapons, and cannot
leave a level without the main player.  However, you can feed potions to your
pet.  This may be useful to augment their speed or attack, or to replenish
their health or mana.

Keep your pet alive! If it dies, it's gone forever.

You can choose to control your main character and/or your pet, by pressing 'a'.
Normally, it is easiest to play the main character only, but in tactically
critical situations, you may want to control both at once (alternating turns
between them).


## Player Stats

If you look at your character's information (by pressing 'c') you will see a
variety of stats.

The most important are your health points (HP) and mana points (MP).  If you
run out of health (below 0) you will die.  Mana is your store of energy used to
cast spells.

'Exp' shows the number of experience points you have; 'Exp next' is the number
needed to reach the next level.  As you go up in levels, you will grow
stronger, and learn to cast new spells.

The four basic stats are strength, dexterity, intelligence, and constitution.
Strength controls how much damage you do for physical attacks.  Dexterity
controls how easy it is to hit (and be hit by) other monsters. Intelligence
controls the amount of mana (spell points) you have to cast spells.  Finally,
constitution controls how many hit points you have.

Attack and defense are discussed under the section "Damage" below.

Speed controls how fast you are relative to other monsters.  Each +1 speed
corresponds to moving approximately 26% faster; A difference of 3 speed
corresponds to moving twice as fast.  Thus for example if you have speed 2 and
a monster has speed 5, it will move twice for every turn you make.   During the
course of your adventures, you may find rings of speed; these are quite
valuable.

## Damage

Physical attacks are expressed in the form "XdY (+W, +Z)".  The damage of any
given attack is computed by summing the result of X random rolls of a Y sided
die, and then adding Z.  The base roll (XdY) is determined by your character's
stats. Wielding weapons modify the bonuses.

The chance of hitting a particular monster is based on the comparison of your
"to Hit" amount +W and the monster's defense. If you look at a monster you can
see the probability that you can hit it and that it can hit you.

Magical attacks never miss, but the damage may be mitigated by gaining
resistances to them.  There are six different magical elements: fire, cold,
acid, electricity, poison, and damage.  As you progress through the game, it is
important to find items to help resist all of these elements, for the magic
that monsters cast is deadly.  Note that poison in particular is dangerous,
because its effect, a slow drain on your health, lasts for several turns.  Note
that wearing multiple items with resistance to the same element will increase
the overall resistance, though there are diminishing returns.

## Spells

All types of players can use magic and cast spells.  Different characters have
different spells they can cast.

Most spells increase in power depending on your stats.  Most spells are based
on your intelligence, but some are based on dexterity, strength, or physical
attack damage.

## Gems

You will find different types of gems throughout the world.  These gems are
imbued with various powers, such as increasing your stats, providing resistance
to a magic element, or improving your physical attack or defense.

Some weapons and armor have one or more sockets where where these gems may be
set.  To do this, you must bring the item and the gem to a jeweler's shop, and
they can upgrade the item for a fee.  Note that this is not reversible, so
choose wisely!  The more powerful the item, the more expensive the upgrade will
be.

## Targeting

In order to cast offensive spells or fire arrows at monsters, it is necessary
to target them first.  There are two ways of doing this.  First, pressing 't'
will allow a particular monster to be targeted.  Such targeting also indicates
to your pet (if you have one) that you wish to attack this monster, which may
influence them to attack this monster as well.

Second, pressing 'T' will allow targeting a particular location on the floor.
This is useful to aim at monsters when there is no direct line of attack, or
when you wish to cancel targeting of all monsters.

# Customization

You can change the window size by dragging the border. You can
change the font and size used by running PoW from the command line.
Note that even non-monospaced fonts work well.

java pow-0.9.0-jar-with-dependencies.jar -Dfont.name="Monaco" -Dfont.size=16

