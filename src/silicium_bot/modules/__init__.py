from .module_base import ModuleBase
from .bot import BotModule
from .jokes import JokesModule
from .shiki import ShikiModule
from .calculator import CalculatorModule
from .info import InfoModule
from .deprecated import DeprecatedModule

cogs = [
    JokesModule,
    CalculatorModule,
    BotModule,
    ShikiModule,
    InfoModule,
    DeprecatedModule
]
