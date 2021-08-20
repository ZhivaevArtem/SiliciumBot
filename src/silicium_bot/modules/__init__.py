from .bot import BotModule
from .jokes import JokesModule
from .shiki import ShikiModule
from .calculator import CalculatorModule
from .info import InfoModule

cogs = [
    JokesModule(),
    CalculatorModule(),
    BotModule(),
    ShikiModule(),
    InfoModule()
]
