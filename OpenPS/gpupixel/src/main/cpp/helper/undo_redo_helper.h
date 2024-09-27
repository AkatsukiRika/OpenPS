/*
* UndoRedoHelper
*
* Created by Rika on 2024/9/26.
* CopyRight © 2024 Rika. All rights reserved.
*/

#pragma once

#include <vector>
#include "openps_record.h"
#include "gpupixel_macros.h"
#include "util.h"

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API UndoRedoHelper {
public:
  void addRecord(const OpenPSRecord& record);

private:
  std::vector<OpenPSRecord> recordList;
  int currentIndex = 0;
};

NS_GPUPIXEL_END