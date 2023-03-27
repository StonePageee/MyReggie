package com.lts.controller;

import com.lts.common.BaseContext;
import com.lts.common.R;
import com.lts.entity.AddressBook;
import com.lts.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     *
     * @param addressBook
     * @param httpSession
     * @return
     */
    @PostMapping
    public R<String> saveAddress(@RequestBody AddressBook addressBook, HttpSession httpSession) {

        Long userId = (Long) httpSession.getAttribute("user");

        if (addressBook != null) {
            addressBook.setUserId(userId);
            addressBookService.save(addressBook);
            return R.success("保存成功！");
        }
        return R.error("保存失败！");
    }

    /**
     * 获取全部地址
     *
     * @param httpSession
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(HttpSession httpSession) {

        Long userId = (Long) httpSession.getAttribute("user");

        List<AddressBook> addressBookList = addressBookService.addressList(userId);

        if (addressBookList != null) {
            return R.success(addressBookList);
        }
        return R.error("暂时没有任何地址，请添加新的收货地址！");
    }

    /**
     * 默认收货地址
     *
     * @param addressBook
     * @param httpSession
     * @return
     */
    @PutMapping("/default")
    public R<String> defaultAddress(@RequestBody AddressBook addressBook, HttpSession httpSession) {

        Long userId = (Long) httpSession.getAttribute("user");
        Long id = addressBook.getId();
        addressBookService.setDefaultAddress(id, userId);

        return R.success("已设置为默认收货地址！");
    }

    /**
     * 获取地址回显
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook> getAddressById(@PathVariable Long id) {

        return R.success(addressBookService.getAddressById(id));
    }

    /**
     * 删除地址
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {

        addressBookService.deleteById(ids);
        return R.success("删除成功！");
    }

    /**
     * 修改地址信息
     *
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> updateAddress(@RequestBody AddressBook addressBook) {

        addressBookService.updateAddress(addressBook);
        return R.success("修改成功！");
    }

    @GetMapping("/default")
    public R<AddressBook> getDefaultAddr(){

        return R.success(addressBookService.getDefaultAddr(BaseContext.getCurrentId()));
    }
}
