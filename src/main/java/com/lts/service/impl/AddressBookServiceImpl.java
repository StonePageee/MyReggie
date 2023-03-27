package com.lts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lts.entity.AddressBook;
import com.lts.mapper.AddressBookMapper;
import com.lts.service.AddressBookService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Override
    public List<AddressBook> addressList(Long userId) {

        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(AddressBook::getUserId,userId)
                .orderByDesc(AddressBook::getIsDefault);

        return this.list(addressBookLambdaQueryWrapper);
    }

    @Override
    public void setDefaultAddress(Long id, Long userId) {
//        一个用户可以有多个收货地址，但只能有一个默认收货地址
//        先查询该用户id下是否有默认地址
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(AddressBook::getUserId,userId);
        List<AddressBook> addressBooks = this.list(addressBookLambdaQueryWrapper);
        List<Integer> isDefaults = addressBooks.stream().map(AddressBook::getIsDefault).collect(Collectors.toList());


        if (isDefaults.contains(1)){
//        如果有默认地址则更改为当前地址id
            for (Integer isDefauts : isDefaults) {
//                将之前的默认地址值修改为0
                if (isDefauts.equals(1)) {
                    LambdaUpdateWrapper<AddressBook> addressBookLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                    addressBookLambdaUpdateWrapper.set(AddressBook::getIsDefault, 0);
                    this.update(addressBookLambdaUpdateWrapper);
                }
            }
//            将传递过来的id设置为默认地址
            LambdaUpdateWrapper<AddressBook> addressBookLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            addressBookLambdaUpdateWrapper.set(AddressBook::getIsDefault,1)
                    .eq(AddressBook::getId,id);
            this.update(addressBookLambdaUpdateWrapper);
        }else {
//        如果没有则将该地址id设为默认
            LambdaUpdateWrapper<AddressBook> addressBookLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            addressBookLambdaUpdateWrapper.set(AddressBook::getIsDefault,1)
                    .eq(AddressBook::getId,id);
            this.update(addressBookLambdaUpdateWrapper);
        }
    }

    @Override
    public AddressBook getAddressById(Long id) {

        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(AddressBook::getId,id);
        return this.getOne(addressBookLambdaQueryWrapper);
    }

    @Override
    public void deleteById(Long id) {

        this.removeById(id);
    }

    @Override
    public void updateAddress(AddressBook addressBook) {
//        根据id查询该条数据
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(AddressBook::getId,addressBook.getId());
        AddressBook addressBookOrigin = this.getOne(addressBookLambdaQueryWrapper);
        if (addressBookOrigin.equals(addressBook)){
//        如果没有更改则直接返回
            return;
        }else {
//        有更改则进行操作
            this.updateById(addressBook);
        }
    }

    @Override
    public AddressBook getDefaultAddr(Long currentId) {

        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(AddressBook::getUserId,currentId);
        addressBookLambdaQueryWrapper.eq(AddressBook::getIsDefault,1);

        return this.getOne(addressBookLambdaQueryWrapper);
    }
}
