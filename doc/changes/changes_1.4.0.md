# hamcrest-resulset-matcher 1.4.0, released 2021-02-09

Code name: Fuzzy matching for Date and Timestamp

## Summary

In some situations you don't care about the datatype but just on the value. In such situations you can use the fuzzy matching. In that mode, the result set matcher does only check the value of the result. In this release we added support for fuzzy matching dates and timestamps. So now in fuzzy matching mode this matcher considers timestamps and dates as equal if they have the same value.

## Features / Enhancements

* # 26: Added fuzzy matching for Date and Timestamp
