package tradly.social.domain.entities

data class AppInitBaseResult(var success:Boolean,var errro:Int,var verifyId:String=Constant.EMPTY,var token:String)
