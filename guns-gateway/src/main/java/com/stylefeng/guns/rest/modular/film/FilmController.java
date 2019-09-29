package com.stylefeng.guns.rest.modular.film;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.stylefeng.guns.api.film.FilmAsyncServiceApi;
import com.stylefeng.guns.api.film.FilmServiceApi;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.rest.modular.film.vo.FilmConditionVO;
import com.stylefeng.guns.rest.modular.film.vo.FilmIndexVO;
import com.stylefeng.guns.rest.modular.film.vo.FilmRequestVO;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/film/")
public class FilmController {

    // todo 这个应该写到配置里去
    private static final String img_pre = "http://img.meetingshop.cn/";

    @Reference(interfaceClass = FilmServiceApi.class,check = false)
    private FilmServiceApi filmServiceApi;

    @Reference(interfaceClass = FilmAsyncServiceApi.class,async = true,check = false)
    private FilmAsyncServiceApi filmAsyncServiceApi;

    // 获取首页信息接口
    /*
        API网关：
            1、功能聚合【API聚合】
            好处：
                1、六个接口，一次请求，同一时刻节省了五次HTTP请求
                2、同一个接口对外暴漏，降低了前后端分离开发的难度和复杂度
            坏处：
                1、一次获取数据过多，容易出现问题
     */
    @GetMapping(value = "getIndex")
    public ResponseVO<FilmIndexVO> getIndex(){

        FilmIndexVO filmIndexVO = new FilmIndexVO();
        // 获取banner信息
        filmIndexVO.setBanners(filmServiceApi.getBanners());
        // 获取正在热映的电影
        filmIndexVO.setHotFilms(filmServiceApi.getHotFilms(true,8,1,1,99,99,99));
        // 即将上映的电影
        filmIndexVO.setSoonFilms(filmServiceApi.getSoonFilms(true,8,1,1,99,99,99));
        // 票房排行榜
        filmIndexVO.setBoxRanking(filmServiceApi.getBoxRanking());
        // 获取受欢迎的榜单
        filmIndexVO.setExpectRanking(filmServiceApi.getExpectRanking());
        // 获取前一百（应该是前10）
        filmIndexVO.setTop100(filmServiceApi.getTop());

        return ResponseVO.success(img_pre,filmIndexVO);
    }


    /**
     * @param catId 分类id
     * @param sourceId 片源id
     * @param yearId 年代id
     * @return com.stylefeng.guns.rest.modular.vo.ResponseVO
     */
    @GetMapping(value = "getConditionList")
    public ResponseVO getConditionList(@RequestParam(name = "catId",required = false,defaultValue = "99")String catId,
                                       @RequestParam(name = "sourceId",required = false,defaultValue = "99")String sourceId,
                                       @RequestParam(name = "yearId",required = false,defaultValue = "99")String yearId){

        FilmConditionVO filmConditionVO = new FilmConditionVO();

        // 标识位
        boolean flag = false;
        // 类型集合
        List<CatVO> cats = filmServiceApi.getCats();
        List<CatVO> catResult = new ArrayList<>();
        CatVO cat = null;
        for(CatVO catVO : cats){
            // 判断集合是否存在catId，如果存在，则将对应的实体变成active状态
            // 6
            // 1,2,3,99,4,5 ->
            /*
                优化：【理论上】
                    1、数据层查询按Id进行排序【有序集合 -> 有序数组】
                    2、通过二分法查找

                todo 此接口应改成, 前端控制active状态, 根据active状态实现发送单个或批量的id，实现多选查询
             */
            if(catVO.getCatId().equals("99")){
                cat = catVO;
                continue;
            }
            if(catVO.getCatId().equals(catId)){
                flag = true;
                catVO.setActive(true);
            }else{
                catVO.setActive(false);
            }
            catResult.add(catVO);
        }
        // 如果不存在，则默认将'全部'这个类别变为Active状态
        if(!flag){
            cat.setActive(true);
            catResult.add(cat);
        }else{
            cat.setActive(false);
            catResult.add(cat);
        }


        // 片源集合
        flag=false;
        List<SourceVO> sources = filmServiceApi.getSources();
        List<SourceVO> sourceResult = new ArrayList<>();
        SourceVO sourceVO = null;
        for(SourceVO source : sources){
            if(source.getSourceId().equals("99")){
                sourceVO = source;
                continue;
            }
            // todo 此处原本是catId，应该是写错了
            if(source.getSourceId().equals(sourceId)){
                flag = true;
                source.setActive(true);
            }else{
                source.setActive(false);
            }
            sourceResult.add(source);
        }
        // 如果不存在，则默认将全部变为Active状态
        if(!flag){
            sourceVO.setActive(true);
            sourceResult.add(sourceVO);
        }else{
            sourceVO.setActive(false);
            sourceResult.add(sourceVO);
        }

        // 年代集合
        flag=false;
        List<YearVO> years = filmServiceApi.getYears();
        List<YearVO> yearResult = new ArrayList<>();
        YearVO yearVO = null;
        for(YearVO year : years){
            if(year.getYearId().equals("99")){
                yearVO = year;
                continue;
            }
            // todo 此处原本是catId，应该是写错了
            if(year.getYearId().equals(yearId)){
                flag = true;
                year.setActive(true);
            }else{
                year.setActive(false);
            }
            yearResult.add(year);
        }
        // 如果不存在，则默认将全部变为Active状态
        if(!flag){
            yearVO.setActive(true);
            yearResult.add(yearVO);
        }else{
            yearVO.setActive(false);
            yearResult.add(yearVO);
        }

        filmConditionVO.setCatInfo(catResult);
        filmConditionVO.setSourceInfo(sourceResult);
        filmConditionVO.setYearInfo(yearResult);

        return ResponseVO.success(filmConditionVO);
    }


    @GetMapping(value = "getFilms")
    public ResponseVO getFilms(FilmRequestVO filmRequestVO){

        String img_pre = "http://img.meetingshop.cn/";

        FilmVO filmVO = null;
        // 根据showType判断影片查询类型
        switch (filmRequestVO.getShowType()){
            case 1 :
                filmVO = filmServiceApi.getHotFilms(
                        false,filmRequestVO.getPageSize(),filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(),filmRequestVO.getSourceId(),filmRequestVO.getYearId(),
                        filmRequestVO.getCatId());
                break;
            case 2 :
                filmVO = filmServiceApi.getSoonFilms(
                        false,filmRequestVO.getPageSize(),filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(),filmRequestVO.getSourceId(),filmRequestVO.getYearId(),
                        filmRequestVO.getCatId());
                break;
            case 3 :
                filmVO = filmServiceApi.getClassicFilms(
                        filmRequestVO.getPageSize(),filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(),filmRequestVO.getSourceId(),
                        filmRequestVO.getYearId(), filmRequestVO.getCatId());
                break;
            default:
                filmVO = filmServiceApi.getHotFilms(
                        false,filmRequestVO.getPageSize(),filmRequestVO.getNowPage(),
                        filmRequestVO.getSortId(),filmRequestVO.getSourceId(),filmRequestVO.getYearId(),
                        filmRequestVO.getCatId());
                break;
        }
        // 根据sortId排序
        // 添加各种条件查询
        // 判断当前是第几页

        // 这些参数都应该封装到一个对象里面
        return ResponseVO.success(
                            filmVO.getNowPage(),filmVO.getTotalPage(),
                            img_pre,filmVO.getFilmInfo());
    }


    /**
     * todo 这个接口不应该模糊查询? 另外一点就是不需要纠结在springcloud里如何使用类似的异步方式
     * todo 因为像这种数据都是缓存到redis里面的，缓存也不是经常需要更新
     * 影片详情查询接口
     * @param searchParam 查询元素
     * @param searchType 1-按名称  2-按ID的查找
     */
    @RequestMapping(value = "films/{searchParam}")
    public ResponseVO films(@PathVariable("searchParam")String searchParam,
                             int searchType) throws ExecutionException, InterruptedException {

        // 根据searchType，判断查询类型
        searchParam = "%";
        FilmDetailVO filmDetail = filmServiceApi.getFilmDetail(searchType, searchParam);

        if(filmDetail==null){
            return ResponseVO.serviceFail("没有可查询的影片");
        }else if(filmDetail.getFilmId()==null || filmDetail.getFilmId().trim().length()==0){
            return ResponseVO.serviceFail("没有可查询的影片");
        }

        String filmId = filmDetail.getFilmId();
        // todo 这块比较重点，Future在javaBasic中有几个练习，dubbo官网也有文档(从v2.7.0开始，Dubbo的所有异步编程接口开始以CompletableFuture为基础)
        // 查询影片的详细信息 -> Dubbo的异步调用
        // 获取影片描述信息
//        FilmDescVO filmDescVO = filmAsyncServiceApi.getFilmDesc(filmId);
        filmAsyncServiceApi.getFilmDesc(filmId);
        Future<FilmDescVO> filmDescVOFuture = RpcContext.getContext().getFuture();
        // 获取图片信息
        filmAsyncServiceApi.getImgs(filmId);
        Future<ImgVO> imgVOFuture = RpcContext.getContext().getFuture();
        // 获取导演信息
        filmAsyncServiceApi.getDectInfo(filmId);
        Future<ActorVO> actorVOFuture = RpcContext.getContext().getFuture();
        // 获取演员信息
        filmAsyncServiceApi.getActors(filmId);
        Future<List<ActorVO>> actorsVOFutrue = RpcContext.getContext().getFuture();

        // 组织info对象
        InfoRequstVO infoRequstVO = new InfoRequstVO();

        // 组织Actor属性
        ActorRequestVO actorRequestVO = new ActorRequestVO();
        actorRequestVO.setActors(actorsVOFutrue.get());
        actorRequestVO.setDirector(actorVOFuture.get());

        // 组织info对象
        infoRequstVO.setActors(actorRequestVO);
        infoRequstVO.setBiography(filmDescVOFuture.get().getBiography());
        infoRequstVO.setFilmId(filmId);
        infoRequstVO.setImgVO(imgVOFuture.get());

        // 组织成返回值
        filmDetail.setInfo04(infoRequstVO);

        return ResponseVO.success("http://img.meetingshop.cn/",filmDetail);
    }

}
